package com.example.reportportal.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SchemaIntrospectionService {

    private final JdbcTemplate jdbcTemplate;

    public SchemaIntrospectionService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String getSchemaContext() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== DATABASE SCHEMA: kellogg_uat ===\n\n");

        // Get all tables with row counts
        List<Map<String, Object>> tables = jdbcTemplate.queryForList(
                "SELECT TABLE_NAME, TABLE_ROWS, TABLE_COMMENT " +
                "FROM information_schema.TABLES " +
                "WHERE TABLE_SCHEMA = 'kellogg_uat' AND TABLE_TYPE = 'BASE TABLE' " +
                "ORDER BY TABLE_NAME"
        );

        for (Map<String, Object> table : tables) {
            String tableName = (String) table.get("TABLE_NAME");
            Object rowCount = table.get("TABLE_ROWS");
            Object comment = table.get("TABLE_COMMENT");

            sb.append("TABLE: ").append(tableName);
            sb.append(" (~").append(rowCount != null ? rowCount : 0).append(" rows)");
            if (comment != null && !comment.toString().isEmpty()) {
                sb.append(" -- ").append(comment);
            }
            sb.append("\n");

            // Get columns for this table
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                    "SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_KEY, COLUMN_COMMENT " +
                    "FROM information_schema.COLUMNS " +
                    "WHERE TABLE_SCHEMA = 'kellogg_uat' AND TABLE_NAME = ? " +
                    "ORDER BY ORDINAL_POSITION",
                    tableName
            );

            for (Map<String, Object> col : columns) {
                sb.append("  - ").append(col.get("COLUMN_NAME"))
                  .append(" ").append(col.get("COLUMN_TYPE"))
                  .append(", nullable=").append(col.get("IS_NULLABLE"));
                String key = (String) col.get("COLUMN_KEY");
                if (key != null && !key.isEmpty()) {
                    sb.append(", key=").append(key);
                }
                sb.append("\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
