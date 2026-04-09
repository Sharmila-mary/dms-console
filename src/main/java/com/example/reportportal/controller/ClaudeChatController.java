package com.example.reportportal.controller;

import com.example.reportportal.model.ChatRequest;
import com.example.reportportal.service.ClaudeAiService;
import com.example.reportportal.service.QueryExecutionService;
import com.example.reportportal.service.RagService;
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

    public ClaudeChatController(ClaudeAiService claudeService,
                                SchemaIntrospectionService schemaService,
                                RagService ragService,
                                QueryExecutionService queryService) {
        this.claudeService = claudeService;
        this.schemaService = schemaService;
        this.ragService = ragService;
        this.queryService = queryService;
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

            // Step 2: Fetch top-8 relevant RAG chunks
            String ragContext = "";
            try {
                ragContext = ragService.searchRelevantChunks(request.getIssue());
            } catch (Exception e) {
                log.warn("RAG search failed: {}", e.getMessage());
            }

            // Step 3: Call Claude with strict JSON format
            Map<String, Object> aiResponse = claudeService.chatAnalyze(
                    request.getIssue(),
                    request.getMessages(),
                    schemaContext,
                    ragContext
            );

            // Check for API error
            if (aiResponse.containsKey("error")) {
                return ResponseEntity.status(500).body(aiResponse);
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
