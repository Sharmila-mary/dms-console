package com.example.reportportal.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class QuerySafetyValidator {

    @Value("${query.max-length:10000}")
    private int maxQueryLength;

    private static final String[] BLOCKED_KEYWORDS = {
            "INSERT", "UPDATE", "DELETE", "DROP", "TRUNCATE", "ALTER", "CREATE",
            "GRANT", "REVOKE", "EXEC", "CALL"
    };

    // Matches SQL single-line comments (--) and block comments (/* ... */)
    private static final Pattern SQL_COMMENT_PATTERN = Pattern.compile(
            "--.*?($|\\n)|/\\*.*?\\*/", Pattern.DOTALL | Pattern.MULTILINE
    );

    /**
     * Validates a SQL query for safety. Throws IllegalArgumentException if unsafe.
     */
    public void validate(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("SQL query cannot be empty.");
        }

        if (sql.length() > maxQueryLength) {
            throw new IllegalArgumentException(
                    "Query exceeds maximum length of " + maxQueryLength + " characters (got " + sql.length() + ").");
        }

        // Block SQL comments
        if (SQL_COMMENT_PATTERN.matcher(sql).find()) {
            throw new IllegalArgumentException("SQL comments (-- or /* */) are not allowed.");
        }

        String upper = sql.trim().toUpperCase();

        // Only allow SELECT and WITH (CTEs)
        if (!upper.startsWith("SELECT") && !upper.startsWith("WITH")) {
            throw new IllegalArgumentException("Only SELECT and WITH (CTE) statements are allowed.");
        }

        // Check for blocked keywords using word boundaries to avoid false positives
        for (String keyword : BLOCKED_KEYWORDS) {
            // Use word boundary check to avoid matching substrings (e.g., "CALLING" shouldn't match "CALL")
            Pattern p = Pattern.compile("\\b" + keyword + "\\b", Pattern.CASE_INSENSITIVE);
            if (p.matcher(sql).find()) {
                throw new IllegalArgumentException("Blocked unsafe SQL keyword: " + keyword);
            }
        }
    }
}
