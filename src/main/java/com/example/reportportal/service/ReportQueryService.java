package com.example.reportportal.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Builds executable MySQL SQL for known report queries by:
 * 1. Extracting the exact SQL text from the uploaded report-queries.xml
 * 2. Running DB lookup queries to resolve complex list params
 * 3. Substituting all :params with real literal values
 *
 * Claude is NOT involved in SQL generation — this is fully deterministic.
 */
@Service
public class ReportQueryService {

    private static final Logger log = LoggerFactory.getLogger(ReportQueryService.class);

    private final JdbcTemplate jdbcTemplate;

    public ReportQueryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Main entry point. Returns a fully executable SQL string, or null if
     * the queryName is not found in the provided XML content.
     */
    public String buildSql(String xmlContent, String queryName, Map<String, Object> extractedParams) {
        if (xmlContent == null || xmlContent.isBlank() || queryName == null || queryName.isBlank()) {
            return null;
        }

        String rawSql = extractSqlFromXml(xmlContent, queryName);
        if (rawSql == null) {
            log.warn("Query '{}' not found in XML content", queryName);
            return null;
        }

        List<String> distrBrCodes = getStringList(extractedParams, "distrBrCodes");
        if (distrBrCodes.isEmpty()) {
            log.warn("Cannot build SQL for query '{}' — no distrBrCodes provided", queryName);
            return null;
        }
        Map<String, List<String>> dbParams = resolveDbParams(distrBrCodes);

        return buildExecutableSql(rawSql, extractedParams, dbParams);
    }

    /**
     * Extracts the raw SQL body for a named <sql-query> from the XML string.
     */
    String extractSqlFromXml(String xmlContent, String queryName) {
        String openTag = "name=\"" + queryName + "\">";
        int start = xmlContent.indexOf(openTag);
        if (start == -1) {
            // Try without namespace prefix
            openTag = "\"" + queryName + "\">";
            start = xmlContent.lastIndexOf(openTag);
            if (start == -1) return null;
        }
        start += openTag.length();
        int end = xmlContent.indexOf("</sql-query>", start);
        if (end == -1) return null;
        String raw = xmlContent.substring(start, end).trim();
        // Decode XML character entities so the SQL is valid for execution
        return raw
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&apos;", "'")
                .replace("&quot;", "\"");
    }

    /**
     * Runs DB lookup queries to resolve params that must be derived from the database.
     */
    Map<String, List<String>> resolveDbParams(List<String> distrBrCodes) {
        Map<String, List<String>> result = new LinkedHashMap<>();

        if (distrBrCodes == null || distrBrCodes.isEmpty()) {
            log.warn("No distrBrCodes provided — DB param resolution will return empty results");
            result.put("DISTRIBUTOR_LST", Collections.emptyList());
            result.put("GEOPROD_LST", Collections.emptyList());
            result.put("LOBCODE_LIST", Collections.emptyList());
            return result;
        }

        String inClause = formatInClause(distrBrCodes);

        // DISTRIBUTOR_LST
        try {
            String sql = "SELECT DISTINCT DistrCode FROM DistributorBranch WHERE DistrBrCode IN (" + inClause + ") AND CmpCode = 'Kellogg'";
            List<String> distrCodes = jdbcTemplate.queryForList(sql, String.class);
            result.put("DISTRIBUTOR_LST", distrCodes);
            log.info("Resolved DISTRIBUTOR_LST: {}", distrCodes);
        } catch (Exception e) {
            log.warn("Failed to resolve DISTRIBUTOR_LST: {}", e.getMessage());
            result.put("DISTRIBUTOR_LST", Collections.emptyList());
        }

        // GEOPROD_LST
        try {
            String sql = "SELECT DISTINCT GeoCode FROM SupplyChainMaster WHERE MemberCode IN (" + inClause + ") AND CmpCode = 'Kellogg'";
            List<String> geoCodes = jdbcTemplate.queryForList(sql, String.class);
            result.put("GEOPROD_LST", geoCodes);
            log.info("Resolved GEOPROD_LST: {}", geoCodes);
        } catch (Exception e) {
            log.warn("Failed to resolve GEOPROD_LST: {}", e.getMessage());
            result.put("GEOPROD_LST", Collections.emptyList());
        }

        // LOBCODE_LIST
        try {
            List<String> lobCodes = jdbcTemplate.queryForList(
                "SELECT LobCode FROM LOBMaster WHERE CmpCode = 'Kellogg'", String.class);
            result.put("LOBCODE_LIST", lobCodes);
            log.info("Resolved LOBCODE_LIST: {}", lobCodes);
        } catch (Exception e) {
            log.warn("Failed to resolve LOBCODE_LIST: {}", e.getMessage());
            result.put("LOBCODE_LIST", Collections.emptyList());
        }

        return result;
    }

    /**
     * Substitutes all :params in the raw SQL with real literal values.
     */
    String buildExecutableSql(String rawSql, Map<String, Object> claudeParams,
                               Map<String, List<String>> dbParams) {
        String sql = rawSql;

        // --- DISTR_BR_LST (user-provided, direct) ---
        List<String> distrBrCodes = getStringList(claudeParams, "distrBrCodes");
        sql = sql.replace(":DISTR_BR_LST", formatInClause(distrBrCodes));

        // --- DISTRIBUTOR_LST (DB lookup) ---
        List<String> distrLst = dbParams.getOrDefault("DISTRIBUTOR_LST", Collections.emptyList());
        sql = sql.replace(":DISTRIBUTOR_LST", distrLst.isEmpty() ? "'__NO_MATCH__'" : formatInClause(distrLst));

        // --- GEOPROD_LST (DB lookup) ---
        List<String> geoLst = dbParams.getOrDefault("GEOPROD_LST", Collections.emptyList());
        sql = sql.replace(":GEOPROD_LST", geoLst.isEmpty() ? "'__NO_MATCH__'" : formatInClause(geoLst));

        // --- LOBCODE_LIST (DB lookup) ---
        List<String> lobLst = dbParams.getOrDefault("LOBCODE_LIST", Collections.emptyList());
        if (lobLst.isEmpty()) {
            // Bypass the LOB filter entirely when no LOBs found
            sql = sql.replace("AND l.LobCode IN (:LOBCODE_LIST)", "AND 1=1");
            sql = sql.replace(":LOBCODE_LIST", "'__NO_MATCH__'");
        } else {
            sql = sql.replace(":LOBCODE_LIST", formatInClause(lobLst));
        }

        // --- GODOWN (user-provided or bypass) ---
        List<String> godownCodes = getStringList(claudeParams, "godownCodes");
        if (godownCodes.isEmpty()) {
            // Bypass godown filter: replace the entire OR condition
            sql = sql.replace("(SO.GodownCode IN (:GODOWN_LST) OR :GODOWN_AVL = 'N')", "1=1");
            sql = sql.replace(":GODOWN_LST", "'__NO_MATCH__'");
            sql = sql.replace(":GODOWN_AVL", "'N'");
        } else {
            sql = sql.replace(":GODOWN_LST", formatInClause(godownCodes));
            sql = sql.replace(":GODOWN_AVL", "'Y'");
        }

        // --- PRODUCT_HIER_VALUE_LIST + PRODUCT_AVL (bypass — set to N) ---
        sql = sql.replace("(p1.ProdHierValCode IN (:PRODUCT_HIER_VALUE_LIST) OR :PRODUCT_AVL = 'N')", "1=1");
        sql = sql.replace(":PRODUCT_HIER_VALUE_LIST", "'__NO_MATCH__'");
        sql = sql.replace(":PRODUCT_AVL", "'N'");

        // --- Product status ---
        String productStatus = getString(claudeParams, "productStatus", "ALL");
        String[] prodStatusVals = resolveStatusValues(productStatus);
        sql = sql.replace(":PRODUCT_STATUS_ACTIVE_STR", "'" + prodStatusVals[0] + "'");
        sql = sql.replace(":PRODUCT_STATUS_INACTIVE_STR", "'" + prodStatusVals[1] + "'");

        // --- Batch status ---
        String batchStatus = getString(claudeParams, "batchStatus", "ALL");
        String[] batchStatusVals = resolveStatusValues(batchStatus);
        sql = sql.replace(":BATCH_STATUS_ACTIVE_STR", "'" + batchStatusVals[0] + "'");
        sql = sql.replace(":BATCH_STATUS_INACTIVE_STR", "'" + batchStatusVals[1] + "'");

        // Warn if any :placeholders remain
        if (sql.contains(":")) {
            log.warn("SQL still contains unresolved placeholders after substitution");
        }

        return sql;
    }

    /**
     * Status mapping:
     * ALL      → active='Y', inactive='N'  → (col='Y' OR col='N') → all rows
     * Active   → active='Y', inactive='Y'  → (col='Y' OR col='Y') → only active
     * Inactive → active='N', inactive='N'  → (col='N' OR col='N') → only inactive
     */
    private String[] resolveStatusValues(String status) {
        if (status == null) return new String[]{"Y", "N"};
        return switch (status.toUpperCase()) {
            case "Y", "ACTIVE"   -> new String[]{"Y", "Y"};
            case "N", "INACTIVE" -> new String[]{"N", "N"};
            default              -> new String[]{"Y", "N"}; // ALL
        };
    }

    /** Formats a list of strings as SQL IN-clause values: 'val1','val2' */
    private String formatInClause(List<String> values) {
        if (values == null || values.isEmpty()) return "''";
        return values.stream()
                .map(v -> "'" + v.replace("'", "''") + "'")
                .collect(Collectors.joining(","));
    }

    @SuppressWarnings("unchecked")
    private List<String> getStringList(Map<String, Object> params, String key) {
        if (params == null) return Collections.emptyList();
        Object val = params.get(key);
        if (val instanceof List<?> list) {
            return list.stream().map(Object::toString).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private String getString(Map<String, Object> params, String key, String defaultValue) {
        if (params == null) return defaultValue;
        Object val = params.get(key);
        return val != null ? val.toString() : defaultValue;
    }
}
