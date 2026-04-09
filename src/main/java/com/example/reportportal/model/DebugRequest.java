package com.example.reportportal.model;

public class DebugRequest {
    private String issueDescription;

    public DebugRequest() {}

    public DebugRequest(String issueDescription) {
        this.issueDescription = issueDescription;
    }

    public String getIssueDescription() { return issueDescription; }
    public void setIssueDescription(String issueDescription) { this.issueDescription = issueDescription; }
}
