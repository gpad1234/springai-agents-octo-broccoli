package com.example.agentdemo.mcp.model;

import java.util.List;

public class MCPToolResult {
    private boolean isError;
    private List<MCPContent> content;

    public MCPToolResult() {
    }

    public MCPToolResult(boolean isError, List<MCPContent> content) {
        this.isError = isError;
        this.content = content;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isError() {
        return isError;
    }

    public void setError(boolean error) {
        isError = error;
    }

    public List<MCPContent> getContent() {
        return content;
    }

    public void setContent(List<MCPContent> content) {
        this.content = content;
    }

    public static class Builder {
        private boolean isError;
        private List<MCPContent> content;

        public Builder isError(boolean isError) {
            this.isError = isError;
            return this;
        }

        public Builder content(List<MCPContent> content) {
            this.content = content;
            return this;
        }

        public MCPToolResult build() {
            return new MCPToolResult(isError, content);
        }
    }
}
