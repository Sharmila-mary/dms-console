package com.example.reportportal.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class QueryExecutionService {

    private static final Logger log = LoggerFactory.getLogger(QueryExecutionService.class);

    private final JdbcTemplate jdbcTemplate;
    private final QuerySafetyValidator safetyValidator;

    @Value("${query.timeout:10}")
    private int queryTimeout;

    @Value("${query.max-rows:200}")
    private int maxRows;

    public QueryExecutionService(JdbcTemplate jdbcTemplate, QuerySafetyValidator safetyValidator) {
        this.jdbcTemplate = jdbcTemplate;
        this.safetyValidator = safetyValidator;
    }

    /**
     * Legacy method — returns flat list of rows (used by DebugController analyze).
     */
    public List<Map<String, Object>> executeQuery(String sql) {
        Map<String, Object> result = executeStructured(sql);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rows = (List<Map<String, Object>>) result.get("rows");
        return rows != null ? rows : Collections.emptyList();
    }

    /**
     * Structured execution — returns { columns, rows, rowCount, truncated }.
     */
    public Map<String, Object> executeStructured(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("SQL query cannot be empty.");
        }

        // Run safety validation
        safetyValidator.validate(sql);

        // Strip trailing semicolons, append LIMIT if missing
        String cleanSql = sql.trim().replaceAll(";\\s*$", "");
        String upper = cleanSql.toUpperCase();
        boolean hasLimit = upper.contains("LIMIT");
        int fetchLimit = maxRows + 1; // fetch one extra to detect truncation

        if (!hasLimit) {
            cleanSql = cleanSql + " LIMIT " + fetchLimit;
        }

        try {
            jdbcTemplate.setQueryTimeout(queryTimeout);
            log.info("Executing SQL (timeout={}s, maxRows={}): {}", queryTimeout, maxRows, cleanSql);

            List<Map<String, Object>> rawRows = jdbcTemplate.queryForList(cleanSql);

            boolean truncated = false;
            if (!hasLimit && rawRows.size() > maxRows) {
                truncated = true;
                rawRows = rawRows.subList(0, maxRows);
            }

            // Extract column names from first row
            List<String> columns = new ArrayList<>();
            if (!rawRows.isEmpty()) {
                columns.addAll(rawRows.get(0).keySet());
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("columns", columns);
            result.put("rows", rawRows);
            result.put("rowCount", rawRows.size());
            result.put("truncated", truncated);

            return result;

        } catch (Exception e) {
            log.error("SQL execution error: {}", e.getMessage(), e);
            throw new RuntimeException("SQL execution failed: " + e.getMessage());
        }
    }
}
