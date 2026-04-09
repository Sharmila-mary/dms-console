package com.example.reportportal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
public class ClaudeAiService {

    private static final Logger log = LoggerFactory.getLogger(ClaudeAiService.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${claude.model}")
    private String model;

    public ClaudeAiService(@Value("${claude.api.url}") String apiUrl,
                           @Value("${claude.api.key}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public String analyzeIssue(String issueDescription, String schemaContext) {
        return analyzeIssue(issueDescription, schemaContext, null);
    }

    public String analyzeIssue(String issueDescription, String schemaContext, String ragContext) {
        try {
            StringBuilder systemPromptBuilder = new StringBuilder();
            systemPromptBuilder.append("""
                You are a DMS (Distribution Management System) debug assistant for the Kellogg distribution system.
                You have access to the database schema below. Your job is to analyze reported issues,
                write diagnostic SQL queries, identify root causes, and suggest fixes.

                IMPORTANT RULES:
                - Only generate SELECT queries (read-only, no mutations)
                - Always use the kellogg_uat schema
                - Be specific and precise in your analysis

                Respond in EXACTLY this structured format:

                **SQL_QUERY:**
                ```sql
                YOUR SELECT QUERY HERE
                ```

                **ROOT_CAUSE:**
                Your root cause analysis here

                **SEVERITY:**
                One of: Critical, High, Medium, Low

                **SUGGESTED_FIX:**
                Your suggested fix here

                DATABASE SCHEMA:
                """);
            systemPromptBuilder.append(schemaContext);

            if (ragContext != null && !ragContext.isBlank()) {
                systemPromptBuilder.append("\n\nSOURCE FILE CONTEXT (from RAG):\n");
                systemPromptBuilder.append("The following are relevant code/config snippets from uploaded source files. ");
                systemPromptBuilder.append("Use this context to better understand the application logic and provide more accurate analysis.\n\n");
                systemPromptBuilder.append(ragContext);
            }

            String systemPrompt = systemPromptBuilder.toString();

            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", model);
            requestBody.put("max_tokens", 4096);
            requestBody.put("system", systemPrompt);

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> userMessage = new LinkedHashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", "Please analyze this issue and provide a diagnostic SQL query:\n\n" + issueDescription);
            messages.add(userMessage);
            requestBody.put("messages", messages);

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            String responseBody = webClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(jsonBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode content = root.path("content");
            if (content.isArray() && content.size() > 0) {
                return content.get(0).path("text").asText();
            }

            return "No response from Claude API";

        } catch (Exception e) {
            log.error("Error calling Claude API: {}", e.getMessage(), e);
            return "Error calling Claude API: " + e.getMessage();
        }
    }

    /**
     * Chat-based analysis that enforces strict JSON output.
     * Supports multi-turn conversation via messages list.
     */
    public Map<String, Object> chatAnalyze(String issue, List<Map<String, String>> conversationMessages,
                                            String schemaContext, String ragContext) {
        try {
            StringBuilder systemPrompt = new StringBuilder();
            systemPrompt.append(
                "You are an expert DMS (Distributor Management System) debug assistant for the Kellogg distribution system (DMS-Kellogg) built by Botree Software.\n\n"
                + "You diagnose live production issues reported by L1/L2/L3 support engineers.\n"
                + "You have access to the database schema and source code context below.\n\n"
                + "STRICT RULES:\n"
                + "1. Only generate SELECT queries — absolutely NO INSERT, UPDATE, DELETE, DROP, or any mutating SQL.\n"
                + "2. Always target the kellogg_uat schema.\n"
                + "3. The SQL must be fully executable on MySQL 8.x — no placeholders, no pseudo-code.\n"
                + "4. Respond ONLY with a single valid JSON object — NO markdown, NO code fences, NO extra text.\n"
                + "5. The JSON must have EXACTLY these keys (all required):\n\n"
                + "{\n"
                + "  \"issue_summary\": \"one sentence description of the issue\",\n"
                + "  \"module\": \"affected DMS module name (e.g., Order Processing, Inventory, Billing, Distribution, Reports)\",\n"
                + "  \"affected_table\": \"main MySQL table involved\",\n"
                + "  \"sql\": \"full executable MySQL SELECT query\",\n"
                + "  \"sql_explanation\": \"what this query will reveal\",\n"
                + "  \"root_cause\": \"most likely root cause of the issue\",\n"
                + "  \"fix\": \"step by step resolution instructions\",\n"
                + "  \"severity\": \"LOW | MEDIUM | HIGH | CRITICAL\"\n"
                + "}\n\n"
                + "IMPORTANT: Output ONLY the JSON object. No markdown. No explanation outside the JSON. No ```json fences.\n\n"
            );

            systemPrompt.append("DATABASE SCHEMA:\n");
            systemPrompt.append(schemaContext);

            if (ragContext != null && !ragContext.isBlank()) {
                systemPrompt.append("\n\nSOURCE CODE CONTEXT (from RAG — uploaded application files):\n");
                systemPrompt.append(ragContext);
            }

            // Build messages array
            List<Map<String, String>> messages = new ArrayList<>();

            // Add conversation history if provided
            if (conversationMessages != null && !conversationMessages.isEmpty()) {
                messages.addAll(conversationMessages);
            }

            // Add the current issue as the latest user message (if not already in conversation)
            if (messages.isEmpty() || !messages.get(messages.size() - 1).get("role").equals("user")) {
                Map<String, String> userMsg = new LinkedHashMap<>();
                userMsg.put("role", "user");
                userMsg.put("content", issue);
                messages.add(userMsg);
            }

            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", model);
            requestBody.put("max_tokens", 4096);
            requestBody.put("system", systemPrompt.toString());
            requestBody.put("messages", messages);

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            log.info("Calling Claude chat API for issue: {}", issue);

            String responseBody = webClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(jsonBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode content = root.path("content");
            if (content.isArray() && content.size() > 0) {
                String text = content.get(0).path("text").asText();
                return parseStrictJson(text);
            }

            return errorResponse("No response from Claude API");

        } catch (Exception e) {
            log.error("Claude chat API error: {}", e.getMessage(), e);
            return errorResponse("Claude API error: " + e.getMessage());
        }
    }

    /**
     * Parse the strict JSON response from Claude, handling edge cases
     * where Claude might wrap it in code fences despite instructions.
     */
    private Map<String, Object> parseStrictJson(String text) {
        if (text == null || text.isBlank()) {
            return errorResponse("Empty response from AI");
        }

        // Strip markdown code fences if Claude added them anyway
        String cleaned = text.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        cleaned = cleaned.trim();

        try {
            JsonNode node = objectMapper.readTree(cleaned);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("issue_summary", node.path("issue_summary").asText(""));
            result.put("module", node.path("module").asText(""));
            result.put("affected_table", node.path("affected_table").asText(""));
            result.put("sql", node.path("sql").asText(""));
            result.put("sql_explanation", node.path("sql_explanation").asText(""));
            result.put("root_cause", node.path("root_cause").asText(""));
            result.put("fix", node.path("fix").asText(""));
            result.put("severity", node.path("severity").asText("MEDIUM"));
            return result;
        } catch (Exception e) {
            log.warn("Failed to parse Claude JSON response, returning raw text. Error: {}", e.getMessage());
            // Return raw text as fallback
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("issue_summary", "See raw analysis below");
            fallback.put("module", "Unknown");
            fallback.put("affected_table", "Unknown");
            fallback.put("sql", "");
            fallback.put("sql_explanation", "");
            fallback.put("root_cause", text);
            fallback.put("fix", "");
            fallback.put("severity", "MEDIUM");
            fallback.put("raw_response", text);
            return fallback;
        }
    }

    private Map<String, Object> errorResponse(String message) {
        Map<String, Object> err = new LinkedHashMap<>();
        err.put("error", message);
        return err;
    }
}
