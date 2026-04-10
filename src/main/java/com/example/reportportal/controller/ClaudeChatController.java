package com.example.reportportal.controller;

import com.example.reportportal.model.ChatRequest;
import com.example.reportportal.service.ClaudeAiService;
import com.example.reportportal.service.QueryExecutionService;
import com.example.reportportal.service.RagService;
import com.example.reportportal.service.ReportQueryService;
import com.example.reportportal.service.SchemaIntrospectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/claude")
@CrossOrigin("http://localhost:3000")
public class ClaudeChatController {

    private static final Logger log = LoggerFactory.getLogger(ClaudeChatController.class);

    private final ClaudeAiService claudeService;
    private final SchemaIntrospectionService schemaService;
    private final RagService ragService;
    private final QueryExecutionService queryService;
    private final ReportQueryService reportQueryService;

    public ClaudeChatController(ClaudeAiService claudeService,
                                SchemaIntrospectionService schemaService,
                                RagService ragService,
                                QueryExecutionService queryService,
                                ReportQueryService reportQueryService) {
        this.claudeService = claudeService;
        this.schemaService = schemaService;
        this.ragService = ragService;
        this.queryService = queryService;
        this.reportQueryService = reportQueryService;
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody ChatRequest request) {
        log.info("Claude chat request: {}", request.getIssue());

        try {
            // Step 1: Get database schema context
            String schemaContext;
            try {
                schemaContext = schemaService.getSchemaContext();
            } catch (Exception e) {
                log.warn("Schema introspection failed (DB may be down): {}", e.getMessage());
                schemaContext = "Schema unavailable — database connection failed.";
            }

            // Step 2: Fetch relevant RAG chunks (for non-XML source files)
            String ragContext = "";
            try {
                ragContext = ragService.searchRelevantChunks(request.getIssue());
            } catch (Exception e) {
                log.warn("RAG search failed: {}", e.getMessage());
            }

            // Step 2b: Assemble full content of all uploaded XML query files
            String fullQueryXml = "";
            try {
                List<String> xmlFiles = ragService.listXmlFileNames();
                StringBuilder xmlBuilder = new StringBuilder();
                for (String xmlFile : xmlFiles) {
                    String content = ragService.getFullFileContent(xmlFile);
                    if (!content.isBlank()) {
                        xmlBuilder.append("=== ").append(xmlFile).append(" ===\n");
                        xmlBuilder.append(content).append("\n\n");
                        log.info("Assembled full XML for: {}", xmlFile);
                    }
                }
                fullQueryXml = xmlBuilder.toString();
            } catch (Exception e) {
                log.warn("Failed to assemble XML query files: {}", e.getMessage());
            }

            // Step 3: Call Claude with strict JSON format
            Map<String, Object> aiResponse = claudeService.chatAnalyze(
                    request.getIssue(),
                    request.getMessages(),
                    schemaContext,
                    ragContext,
                    fullQueryXml
            );

            // Check for API error
            if (aiResponse.containsKey("error")) {
                return ResponseEntity.status(500).body(aiResponse);
            }

            // Step 3b: If Claude identified a known report query, build SQL deterministically
            // from the XML + DB lookups — do NOT use Claude's sql field for this
            String queryName = (String) aiResponse.get("query_name");
            @SuppressWarnings("unchecked")
            Map<String, Object> extractedParams = (Map<String, Object>) aiResponse.get("extracted_params");

            if (queryName != null && !queryName.isBlank() && !fullQueryXml.isBlank()) {
                try {
                    String builtSql = reportQueryService.buildSql(fullQueryXml, queryName, extractedParams);
                    if (builtSql != null) {
                        aiResponse.put("sql", builtSql);
                        log.info("Built SQL from XML for query: {}", queryName);
                    } else {
                        // buildSql returned null — likely missing distrBrCodes
                        java.util.List<?> codes = extractedParams != null
                                ? (java.util.List<?>) extractedParams.getOrDefault("distrBrCodes", java.util.List.of())
                                : java.util.List.of();
                        if (codes.isEmpty()) {
                            aiResponse.put("warning", "No distributor branch code found in your description. "
                                    + "Please include the branch code (e.g. KLDEL01) so the report query can be executed.");
                        }
                    }
                } catch (Exception e) {
                    log.warn("ReportQueryService failed, falling back to Claude sql: {}", e.getMessage());
                }
            } else if (queryName == null || queryName.isBlank()) {
                // No matching query in XML — explain why there's no SQL
                aiResponse.put("sql_explanation",
                        "No matching report query found in the uploaded XML files for this issue type. "
                        + "The diagnosis above is based on schema context only. "
                        + "Upload the relevant query XML file to enable SQL execution for this report.");
            }

            // Step 4: Auto-execute the SQL if present
            String sql = (String) aiResponse.get("sql");
            Map<String, Object> queryResult = null;
            if (sql != null && !sql.isBlank()) {
                try {
                    queryResult = queryService.executeStructured(sql);
                } catch (IllegalArgumentException e) {
                    log.warn("SQL validation failed: {}", e.getMessage());
                    queryResult = Map.of("error", "SQL validation failed: " + e.getMessage());
                } catch (Exception e) {
                    log.warn("SQL execution failed: {}", e.getMessage());
                    queryResult = Map.of("error", "SQL execution failed: " + e.getMessage());
                }
            }

            // Step 5: Build response
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("analysis", aiResponse);
            if (queryResult != null) {
                response.put("query_result", queryResult);
            }
            response.put("rag_chunks_used", ragContext != null && !ragContext.isBlank());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Claude chat failed: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Chat analysis failed: " + e.getMessage()));
        }
    }
}
