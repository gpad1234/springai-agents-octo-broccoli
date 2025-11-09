package com.example.agentdemo.mcp.model;

import java.util.Map;

public class MCPToolCall {
    private String name;
    private Map<String, Object> arguments;

    public MCPToolCall() {
    }

    public MCPToolCall(String name, Map<String, Object> arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }

    public void setArguments(Map<String, Object> arguments) {
        this.arguments = arguments;
    }

    public static class Builder {
        private String name;
        private Map<String, Object> arguments;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder arguments(Map<String, Object> arguments) {
            this.arguments = arguments;
            return this;
        }

        public MCPToolCall build() {
            return new MCPToolCall(name, arguments);
        }
    }
}
