package com.example.agentdemo.mcp.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRpcResponse {
    private String jsonrpc;
    private String id;
    private Object result;
    private JsonRpcError error;

    public JsonRpcResponse() {
    }

    public JsonRpcResponse(String jsonrpc, String id, Object result, JsonRpcError error) {
        this.jsonrpc = jsonrpc;
        this.id = id;
        this.result = result;
        this.error = error;
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

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public JsonRpcError getError() {
        return error;
    }

    public void setError(JsonRpcError error) {
        this.error = error;
    }
}
