package com.example.reportportal.model;

import java.util.List;
import java.util.Map;

public class ChatRequest {

    private String issue;
    private List<Map<String, String>> messages;

    public ChatRequest() {}

    public String getIssue() { return issue; }
    public void setIssue(String issue) { this.issue = issue; }

    public List<Map<String, String>> getMessages() { return messages; }
    public void setMessages(List<Map<String, String>> messages) { this.messages = messages; }
}
