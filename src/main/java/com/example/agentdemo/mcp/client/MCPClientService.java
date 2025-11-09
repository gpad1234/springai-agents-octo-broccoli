package com.example.agentdemo.mcp.client;

import com.example.agentdemo.mcp.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MCP Client Service for communicating with MCP servers via stdio
 * Implements the Model Context Protocol for tool discovery and execution
 */
@Service
public class MCPClientService {
    
    private static final Logger log = LoggerFactory.getLogger(MCPClientService.class);
    
    private final ObjectMapper objectMapper;
    private final Map<String, MCPServerConnection> serverConnections;
    private final AtomicInteger requestIdCounter;
    
    public MCPClientService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.serverConnections = new ConcurrentHashMap<>();
        this.requestIdCounter = new AtomicInteger(0);
    }
    
    /**
     * Connect to an MCP server
     */
    public void connectServer(String serverName, String command, List<String> args) throws IOException {
        log.info("Connecting to MCP server: {} with command: {}", serverName, command);
        
        ProcessBuilder processBuilder = new ProcessBuilder();
        List<String> commandList = new ArrayList<>();
        commandList.add(command);
        if (args != null) {
            commandList.addAll(args);
        }
        processBuilder.command(commandList);
        
        Process process = processBuilder.start();
        
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        
        MCPServerConnection connection = new MCPServerConnection(serverName, process, reader, writer);
        serverConnections.put(serverName, connection);
        
        // Initialize the connection
        initialize(serverName);
        
        log.info("Successfully connected to MCP server: {}", serverName);
    }
    
    /**
     * Initialize MCP connection with the server
     */
    private void initialize(String serverName) throws IOException {
        MCPServerConnection connection = serverConnections.get(serverName);
        if (connection == null) {
            throw new IllegalStateException("No connection found for server: " + serverName);
        }
        
        Map<String, Object> params = new HashMap<>();
        params.put("protocolVersion", "2024-11-05");
        params.put("capabilities", Map.of("tools", Map.of()));
        params.put("clientInfo", Map.of(
            "name", "spring-ai-agent",
            "version", "1.0.0"
        ));
        
        JsonRpcRequest request = JsonRpcRequest.builder()
            .jsonrpc("2.0")
            .id(String.valueOf(requestIdCounter.incrementAndGet()))
            .method("initialize")
            .params(params)
            .build();
        
        sendRequest(connection, request);
        JsonRpcResponse response = readResponse(connection);
        
        if (response.getError() != null) {
            throw new IOException("Failed to initialize: " + response.getError().getMessage());
        }
        
        log.info("Initialized MCP server: {}", serverName);
    }
    
    /**
     * List available tools from a server
     */
    public List<MCPTool> listTools(String serverName) throws IOException {
        MCPServerConnection connection = serverConnections.get(serverName);
        if (connection == null) {
            throw new IllegalStateException("No connection found for server: " + serverName);
        }
        
        JsonRpcRequest request = JsonRpcRequest.builder()
            .jsonrpc("2.0")
            .id(String.valueOf(requestIdCounter.incrementAndGet()))
            .method("tools/list")
            .params(Collections.emptyMap())
            .build();
        
        sendRequest(connection, request);
        JsonRpcResponse response = readResponse(connection);
        
        if (response.getError() != null) {
            throw new IOException("Failed to list tools: " + response.getError().getMessage());
        }
        
        // Parse tools from response
        Map<String, Object> result = (Map<String, Object>) response.getResult();
        List<Map<String, Object>> toolsData = (List<Map<String, Object>>) result.get("tools");
        
        List<MCPTool> tools = new ArrayList<>();
        for (Map<String, Object> toolData : toolsData) {
            MCPTool tool = MCPTool.builder()
                .name((String) toolData.get("name"))
                .description((String) toolData.get("description"))
                .inputSchema((Map<String, Object>) toolData.get("inputSchema"))
                .build();
            tools.add(tool);
        }
        
        return tools;
    }
    
    /**
     * Call a tool on an MCP server
     */
    public MCPToolResult callTool(String serverName, MCPToolCall toolCall) throws IOException {
        MCPServerConnection connection = serverConnections.get(serverName);
        if (connection == null) {
            throw new IllegalStateException("No connection found for server: " + serverName);
        }
        
        Map<String, Object> params = new HashMap<>();
        params.put("name", toolCall.getName());
        params.put("arguments", toolCall.getArguments());
        
        JsonRpcRequest request = JsonRpcRequest.builder()
            .jsonrpc("2.0")
            .id(String.valueOf(requestIdCounter.incrementAndGet()))
            .method("tools/call")
            .params(params)
            .build();
        
        sendRequest(connection, request);
        JsonRpcResponse response = readResponse(connection);
        
        if (response.getError() != null) {
            return MCPToolResult.builder()
                .isError(true)
                .content(List.of(MCPContent.builder()
                    .type("text")
                    .text("Error: " + response.getError().getMessage())
                    .build()))
                .build();
        }
        
        // Parse result
        Map<String, Object> result = (Map<String, Object>) response.getResult();
        List<Map<String, Object>> contentData = (List<Map<String, Object>>) result.get("content");
        
        List<MCPContent> content = new ArrayList<>();
        for (Map<String, Object> contentItem : contentData) {
            MCPContent mcpContent = MCPContent.builder()
                .type((String) contentItem.get("type"))
                .text((String) contentItem.get("text"))
                .mimeType((String) contentItem.get("mimeType"))
                .data(contentItem.get("data"))
                .build();
            content.add(mcpContent);
        }
        
        return MCPToolResult.builder()
            .isError(false)
            .content(content)
            .build();
    }
    
    /**
     * Disconnect from a server
     */
    public void disconnectServer(String serverName) {
        MCPServerConnection connection = serverConnections.remove(serverName);
        if (connection != null) {
            try {
                connection.getWriter().close();
                connection.getReader().close();
                connection.getProcess().destroy();
                log.info("Disconnected from MCP server: {}", serverName);
            } catch (IOException e) {
                log.error("Error closing connection to server: {}", serverName, e);
            }
        }
    }
    
    /**
     * Disconnect from all servers
     */
    public void disconnectAll() {
        for (String serverName : new ArrayList<>(serverConnections.keySet())) {
            disconnectServer(serverName);
        }
    }
    
    private void sendRequest(MCPServerConnection connection, JsonRpcRequest request) throws IOException {
        String json = objectMapper.writeValueAsString(request);
        log.debug("Sending request: {}", json);
        connection.getWriter().write(json);
        connection.getWriter().newLine();
        connection.getWriter().flush();
    }
    
    private JsonRpcResponse readResponse(MCPServerConnection connection) throws IOException {
        String line = connection.getReader().readLine();
        if (line == null) {
            throw new IOException("Connection closed by server");
        }
        log.debug("Received response: {}", line);
        return objectMapper.readValue(line, JsonRpcResponse.class);
    }
    
    /**
     * Inner class to hold server connection details
     */
    private static class MCPServerConnection {
        private final String serverName;
        private final Process process;
        private final BufferedReader reader;
        private final BufferedWriter writer;
        
        public MCPServerConnection(String serverName, Process process, BufferedReader reader, BufferedWriter writer) {
            this.serverName = serverName;
            this.process = process;
            this.reader = reader;
            this.writer = writer;
        }
        
        public String getServerName() { return serverName; }
        public Process getProcess() { return process; }
        public BufferedReader getReader() { return reader; }
        public BufferedWriter getWriter() { return writer; }
    }
}
