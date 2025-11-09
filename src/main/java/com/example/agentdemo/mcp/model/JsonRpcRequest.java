package com.example.agentdemo.mcp.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRpcRequest {
    private String jsonrpc = "2.0";
    private String id;
    private String method;
    private Object params;

    public JsonRpcRequest() {
    }

    public JsonRpcRequest(String jsonrpc, String id, String method, Object params) {
        this.jsonrpc = jsonrpc;
        this.id = id;
        this.method = method;
        this.params = params;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Object getParams() {
        return params;
    }

    public void setParams(Object params) {
        this.params = params;
    }

    public static class Builder {
        private String jsonrpc = "2.0";
        private String id;
        private String method;
        private Object params;

        public Builder jsonrpc(String jsonrpc) {
            this.jsonrpc = jsonrpc;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder params(Object params) {
            this.params = params;
            return this;
        }

        public JsonRpcRequest build() {
            return new JsonRpcRequest(jsonrpc, id, method, params);
        }
    }
}
