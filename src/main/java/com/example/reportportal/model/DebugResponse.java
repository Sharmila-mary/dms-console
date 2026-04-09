package com.example.reportportal.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class DebugResponse {
    private String issueDescription;
    private String schemaContext;
    private String aiAnalysis;
    private String generatedSql;
    private List<Map<String, Object>> queryResults;
    private String rootCause;
    private String suggestedFix;
    private String severity;
    private LocalDateTime timestamp;

    public DebugResponse() {}

    public String getIssueDescription() { return issueDescription; }
    public void setIssueDescription(String issueDescription) { this.issueDescription = issueDescription; }

    public String getSchemaContext() { return schemaContext; }
    public void setSchemaContext(String schemaContext) { this.schemaContext = schemaContext; }

    public String getAiAnalysis() { return aiAnalysis; }
    public void setAiAnalysis(String aiAnalysis) { this.aiAnalysis = aiAnalysis; }

    public String getGeneratedSql() { return generatedSql; }
    public void setGeneratedSql(String generatedSql) { this.generatedSql = generatedSql; }

    public List<Map<String, Object>> getQueryResults() { return queryResults; }
    public void setQueryResults(List<Map<String, Object>> queryResults) { this.queryResults = queryResults; }

    public String getRootCause() { return rootCause; }
    public void setRootCause(String rootCause) { this.rootCause = rootCause; }

    public String getSuggestedFix() { return suggestedFix; }
    public void setSuggestedFix(String suggestedFix) { this.suggestedFix = suggestedFix; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
