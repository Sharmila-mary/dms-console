package com.example.reportportal.controller;

import com.example.reportportal.model.DebugRequest;
import com.example.reportportal.model.DebugResponse;
import com.example.reportportal.model.QueryRequest;
import com.example.reportportal.service.ClaudeAiService;
import com.example.reportportal.service.QueryExecutionService;
import com.example.reportportal.service.RagService;
import com.example.reportportal.service.SchemaIntrospectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/debug")
@CrossOrigin("http://localhost:3000")
public class DebugController {

    private static final Logger log = LoggerFactory.getLogger(DebugController.class);

    private final SchemaIntrospectionService schemaService;
    private final ClaudeAiService claudeService;
    private final QueryExecutionService queryService;
    private final RagService ragService;

    public DebugController(SchemaIntrospectionService schemaService,
                           ClaudeAiService claudeService,
                           QueryExecutionService queryService,
                           RagService ragService) {
        this.schemaService = schemaService;
        this.claudeService = claudeService;
        this.queryService = queryService;
        this.ragService = ragService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<DebugResponse> analyze(@RequestBody DebugRequest request) {
        log.info("Debug analysis requested: {}", request.getIssueDescription());

        DebugResponse response = new DebugResponse();
        response.setIssueDescription(request.getIssueDescription());
        response.setTimestamp(LocalDateTime.now());

        try {
            // Step 1: Get schema context
            String schemaContext = schemaService.getSchemaContext();
            response.setSchemaContext(schemaContext);

            // Step 2: Search RAG chunks for relevant context
            String ragContext = ragService.searchRelevantChunks(request.getIssueDescription());

            // Step 3: Call Claude AI with RAG context
            String aiAnalysis = claudeService.analyzeIssue(request.getIssueDescription(), schemaContext, ragContext);
            response.setAiAnalysis(aiAnalysis);

            // Step 4: Parse structured sections from AI response
            response.setGeneratedSql(extractSql(aiAnalysis));
            response.setRootCause(extractSection(aiAnalysis, "ROOT_CAUSE"));
            response.setSeverity(extractSection(aiAnalysis, "SEVERITY"));
            response.setSuggestedFix(extractSection(aiAnalysis, "SUGGESTED_FIX"));

            // Step 5: Execute the extracted SQL
            String sql = response.getGeneratedSql();
            if (sql != null && !sql.isBlank()) {
                try {
                    List<Map<String, Object>> results = queryService.executeQuery(sql);
                    response.setQueryResults(results);
                } catch (Exception e) {
                    log.warn("SQL execution failed: {}", e.getMessage());
                    response.setQueryResults(new ArrayList<Map<String, Object>>());
                }
            } else {
                response.setQueryResults(new ArrayList<Map<String, Object>>());
            }

        } catch (Exception e) {
            log.error("Debug analysis failed: {}", e.getMessage(), e);
            response.setAiAnalysis("Analysis failed: " + e.getMessage());
            response.setQueryResults(new ArrayList<Map<String, Object>>());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/schema")
    public ResponseEntity<Map<String, String>> getSchema() {
        String schema = schemaService.getSchemaContext();
        return ResponseEntity.ok(Map.of("schema", schema));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("status", "UP");
        status.put("service", "DMS AI Debug Console");
        status.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(status);
    }

    // --- Report Download Endpoint ---

    @SuppressWarnings("unchecked")
    @PostMapping("/report")
    public ResponseEntity<byte[]> downloadReport(@RequestBody Map<String, Object> payload) {
        try {
            Map<String, Object> analysis = (Map<String, Object>) payload.get("analysis");
            Map<String, Object> queryResult = (Map<String, Object>) payload.get("query_result");

            StringBuilder sb = new StringBuilder();
            sb.append("================================================================================\n");
            sb.append("                   DMS AI DEBUG CONSOLE — DIAGNOSIS REPORT\n");
            sb.append("================================================================================\n\n");

            sb.append("Timestamp       : ").append(LocalDateTime.now()).append("\n");
            sb.append("Module          : ").append(val(analysis, "module")).append("\n");
            sb.append("Severity        : ").append(val(analysis, "severity")).append("\n");
            sb.append("Affected Table  : ").append(val(analysis, "affected_table")).append("\n");
            sb.append("\n--------------------------------------------------------------------------------\n");
            sb.append("ISSUE SUMMARY\n");
            sb.append("--------------------------------------------------------------------------------\n");
            sb.append(val(analysis, "issue_summary")).append("\n");

            sb.append("\n--------------------------------------------------------------------------------\n");
            sb.append("DIAGNOSTIC SQL QUERY\n");
            sb.append("--------------------------------------------------------------------------------\n");
            sb.append(val(analysis, "sql")).append("\n");

            sb.append("\n--------------------------------------------------------------------------------\n");
            sb.append("SQL EXPLANATION\n");
            sb.append("--------------------------------------------------------------------------------\n");
            sb.append(val(analysis, "sql_explanation")).append("\n");

            sb.append("\n--------------------------------------------------------------------------------\n");
            sb.append("QUERY RESULTS\n");
            sb.append("--------------------------------------------------------------------------------\n");
            if (queryResult != null && queryResult.containsKey("columns")) {
                List<String> columns = (List<String>) queryResult.get("columns");
                List<Map<String, Object>> rows = (List<Map<String, Object>>) queryResult.get("rows");
                int rowCount = queryResult.get("rowCount") != null ? ((Number) queryResult.get("rowCount")).intValue() : 0;
                boolean truncated = queryResult.get("truncated") != null && (boolean) queryResult.get("truncated");

                // Header row
                sb.append(String.join("\t", columns)).append("\n");
                sb.append("-".repeat(columns.size() * 20)).append("\n");

                // Data rows
                if (rows != null) {
                    for (Map<String, Object> row : rows) {
                        for (int i = 0; i < columns.size(); i++) {
                            if (i > 0) sb.append("\t");
                            Object v = row.get(columns.get(i));
                            sb.append(v != null ? v.toString() : "NULL");
                        }
                        sb.append("\n");
                    }
                }

                sb.append("\nTotal rows: ").append(rowCount);
                if (truncated) sb.append(" (truncated to 200 rows)");
                sb.append("\n");
            } else if (queryResult != null && queryResult.containsKey("error")) {
                sb.append("Query execution error: ").append(queryResult.get("error")).append("\n");
            } else {
                sb.append("No query results available.\n");
            }

            sb.append("\n--------------------------------------------------------------------------------\n");
            sb.append("ROOT CAUSE ANALYSIS\n");
            sb.append("--------------------------------------------------------------------------------\n");
            sb.append(val(analysis, "root_cause")).append("\n");

            sb.append("\n--------------------------------------------------------------------------------\n");
            sb.append("RESOLUTION STEPS\n");
            sb.append("--------------------------------------------------------------------------------\n");
            sb.append(val(analysis, "fix")).append("\n");

            sb.append("\n================================================================================\n");
            sb.append("  Generated by DMS AI Support Assistant (Botree Software)\n");
            sb.append("================================================================================\n");

            byte[] content = sb.toString().getBytes(StandardCharsets.UTF_8);
            String filename = "DMS_Debug_Report_" + LocalDateTime.now().toString().replace(":", "-").substring(0, 19) + ".txt";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.TEXT_PLAIN)
                    .contentLength(content.length)
                    .body(content);

        } catch (Exception e) {
            log.error("Report generation failed: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(("Report generation failed: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }

    private String val(Map<String, Object> map, String key) {
        if (map == null) return "N/A";
        Object v = map.get(key);
        return v != null ? v.toString() : "N/A";
    }

    // --- Query Execution Endpoint ---

    @PostMapping("/execute")
    public ResponseEntity<?> executeQuery(@RequestBody QueryRequest request) {
        try {
            Map<String, Object> result = queryService.executeStructured(request.getSql());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn("Query validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Query execution failed: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // --- RAG Endpoints ---

    @PostMapping("/rag/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            ragService.ingestFile(fileName, content);
            return ResponseEntity.ok(Map.of("message", "File '" + fileName + "' uploaded and ingested successfully"));
        } catch (Exception e) {
            log.error("File upload failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    @GetMapping("/rag/files")
    public ResponseEntity<List<Map<String, Object>>> listFiles() {
        return ResponseEntity.ok(ragService.listFiles());
    }

    @GetMapping("/rag/search")
    public ResponseEntity<Map<String, String>> searchRag(@RequestParam("q") String query) {
        String result = ragService.searchRelevantChunks(query);
        return ResponseEntity.ok(Map.of("context", result));
    }

    @DeleteMapping("/rag/files/{fileName}")
    public ResponseEntity<Map<String, String>> deleteFile(@PathVariable String fileName) {
        ragService.deleteFile(fileName);
        return ResponseEntity.ok(Map.of("message", "File '" + fileName + "' deleted successfully"));
    }

    // --- Helper methods ---

    private String extractSql(String text) {
        if (text == null) return null;

        Pattern sqlBlock = Pattern.compile("```sql\\s*\\n?(.*?)\\n?```", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = sqlBlock.matcher(text);
        if (m.find()) {
            return m.group(1).trim();
        }

        Pattern generic = Pattern.compile("SQL_QUERY[:\\s]*.*?```\\s*\\n?(.*?)\\n?```", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        m = generic.matcher(text);
        if (m.find()) {
            return m.group(1).trim();
        }

        Pattern inline = Pattern.compile("(SELECT\\s+.+?;)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        m = inline.matcher(text);
        if (m.find()) {
            return m.group(1).trim();
        }

        return null;
    }

    private String extractSection(String text, String sectionName) {
        if (text == null) return null;

        Pattern p = Pattern.compile("\\*\\*" + sectionName + ":\\*\\*\\s*\\n?(.*?)(?=\\n\\*\\*[A-Z_]+:|$)",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        if (m.find()) {
            return m.group(1).trim();
        }

        p = Pattern.compile(sectionName + ":\\s*\\n?(.*?)(?=\\n[A-Z_]+:|$)",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        m = p.matcher(text);
        if (m.find()) {
            return m.group(1).trim();
        }

        return null;
    }
}
