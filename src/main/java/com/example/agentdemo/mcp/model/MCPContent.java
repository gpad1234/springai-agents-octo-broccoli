package com.example.agentdemo.mcp.model;

public class MCPContent {
    private String type; // "text", "image", "resource"
    private String text;
    private String mimeType;
    private Object data;

    public MCPContent() {
    }

    public MCPContent(String type, String text, String mimeType, Object data) {
        this.type = type;
        this.text = text;
        this.mimeType = mimeType;
        this.data = data;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public static class Builder {
        private String type;
        private String text;
        private String mimeType;
        private Object data;

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public Builder mimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        public Builder data(Object data) {
            this.data = data;
            return this;
        }

        public MCPContent build() {
            return new MCPContent(type, text, mimeType, data);
        }
    }
}
