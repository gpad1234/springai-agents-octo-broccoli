package com.example.agentdemo.agent.skills;

import com.example.agentdemo.agent.Skill;
import com.example.agentdemo.mcp.client.MCPClientService;
import com.example.agentdemo.mcp.model.MCPContent;
import com.example.agentdemo.mcp.model.MCPToolCall;
import com.example.agentdemo.mcp.model.MCPToolResult;
import com.example.agentdemo.model.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Skill that uses MCP to query system information via osquery
 * Example queries:
 * - "osquery: list running processes"
 * - "osquery: show system info"
 * - "osquery: get network connections"
 */
@Component
public class OsqueryMCPSkill implements Skill {
    
    private static final Logger log = LoggerFactory.getLogger(OsqueryMCPSkill.class);
    private static final String SERVER_NAME = "osquery";
    
    @Autowired
    private MCPClientService mcpClient;
    
    @Value("${mcp.osquery.enabled:false}")
    private boolean enabled;
    
    @Value("${mcp.osquery.command:npx}")
    private String command;
    
    @Value("${mcp.osquery.args:-y @modelcontextprotocol/server-osquery}")
    private String args;
    
    private boolean connected = false;
    
    @PostConstruct
    public void init() {
        if (!enabled) {
            log.info("OsqueryMCPSkill is disabled");
            return;
        }
        
        try {
            log.info("Connecting to osquery MCP server...");
            List<String> argsList = args != null ? List.of(args.split("\\s+")) : List.of();
            mcpClient.connectServer(SERVER_NAME, command, argsList);
            connected = true;
            
            // List available tools
            var tools = mcpClient.listTools(SERVER_NAME);
            log.info("Available osquery tools: {}", 
                tools.stream().map(t -> t.getName()).collect(Collectors.joining(", ")));
        } catch (Exception e) {
            log.error("Failed to connect to osquery MCP server", e);
            connected = false;
        }
    }
    
    @PreDestroy
    public void cleanup() {
        if (connected) {
            log.info("Disconnecting from osquery MCP server");
            mcpClient.disconnectServer(SERVER_NAME);
        }
    }
    
    @Override
    public boolean canHandle(String goal) {
        if (!enabled || !connected) {
            return false;
        }
        
        if (goal == null) {
            return false;
        }
        
        String g = goal.toLowerCase();
        return g.startsWith("osquery:") || 
               g.contains("system info") ||
               g.contains("running process") ||
               g.contains("network connection") ||
               g.contains("list users") ||
               g.contains("system query") ||
               g.contains("hostname") ||
               g.contains("what is the") && (g.contains("system") || g.contains("computer"));
    }
    
    @Override
    public ActionResult execute(String goal) {
        if (!connected) {
            return new ActionResult(false, "OsqueryMCPSkill", 
                "Not connected to osquery MCP server");
        }
        
        try {
            // Extract the query from the goal
            String query = extractQuery(goal);
            
            // Determine which osquery tool to use based on the query
            String toolName = determineToolName(query);
            Map<String, Object> arguments = buildArguments(query);
            
            // Call the MCP tool
            MCPToolCall toolCall = MCPToolCall.builder()
                .name(toolName)
                .arguments(arguments)
                .build();
            
            MCPToolResult result = mcpClient.callTool(SERVER_NAME, toolCall);
            
            if (result.isError()) {
                return new ActionResult(false, "OsqueryMCPSkill", 
                    extractTextFromContent(result.getContent()));
            }
            
            String output = extractTextFromContent(result.getContent());
            return new ActionResult(true, "OsqueryMCPSkill", output);
            
        } catch (Exception e) {
            log.error("Error executing osquery", e);
            return new ActionResult(false, "OsqueryMCPSkill", 
                "Error: " + e.getMessage());
        }
    }
    
    private String extractQuery(String goal) {
        if (goal.toLowerCase().startsWith("osquery:")) {
            return goal.substring(goal.indexOf(":") + 1).trim();
        }
        return goal;
    }
    
    private String determineToolName(String query) {
        // The Python osquery MCP server only has one tool: query_osquery
        return "query_osquery";
    }
    
    private Map<String, Object> buildArguments(String query) {
        Map<String, Object> args = new HashMap<>();
        
        String q = query.toLowerCase();
        
        // Build SQL query based on the request
        if (q.contains("process")) {
            args.put("sql", "SELECT pid, name, path, cmdline FROM processes LIMIT 20");
        } else if (q.contains("system info") || q.contains("system_info")) {
            args.put("sql", "SELECT * FROM system_info");
        } else if (q.contains("hostname")) {
            args.put("sql", "SELECT hostname FROM system_info");
        } else if (q.contains("network") || q.contains("connection")) {
            args.put("sql", "SELECT pid, local_address, local_port, remote_address, remote_port, state FROM process_open_sockets LIMIT 20");
        } else if (q.contains("user")) {
            args.put("sql", "SELECT uid, username, shell FROM users");
        } else {
            // Try to use the query as-is if it looks like SQL
            args.put("sql", query);
        }
        
        return args;
    }
    
    private String extractTextFromContent(List<MCPContent> content) {
        if (content == null || content.isEmpty()) {
            return "No content returned";
        }
        
        return content.stream()
            .filter(c -> "text".equals(c.getType()))
            .map(MCPContent::getText)
            .collect(Collectors.joining("\n"));
    }
}
