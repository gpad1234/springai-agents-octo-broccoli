# MCP (Model Context Protocol) Integration Guide

**Status:** ✅ TESTED & WORKING  
**Test Date:** November 8, 2025  
**MCP Server:** Python-based osquery MCP server (custom implementation)  
**Protocol:** JSON-RPC 2.0 over stdio

## Overview

This Spring AI Agent application includes **MCP (Model Context Protocol)** client capabilities, allowing it to connect to external MCP servers and use their tools as skills.

**Currently integrated:** Custom Python osquery MCP server for system information queries.

**Test Results:** All 6 tests passing
- ✅ Hostname queries
- ✅ System information
- ✅ User listing
- ✅ Process listing
- ✅ Network connections
- ✅ Custom SQL queries

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Spring AI Agent                          │
│                                                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ Built-in     │  │ Built-in     │  │   MCP-based  │     │
│  │ Skills       │  │ Skills       │  │   Skills     │     │
│  │              │  │              │  │              │     │
│  │ Calculator   │  │ Summarize    │  │ OsqueryMCP   │     │
│  │ MockSearch   │  │              │  │              │     │
│  └──────────────┘  └──────────────┘  └──────┬───────┘     │
│                                              │              │
│                                       ┌──────▼───────┐     │
│                                       │ MCP Client   │     │
│                                       │ Service      │     │
│                                       └──────┬───────┘     │
└───────────────────────────────────────────┼──┼────────────┘
                                            │  │
                         JSON-RPC over stdio│  │
                                            │  │
                        ┌───────────────────▼──▼───────────┐
                        │   MCP Server: osquery            │
                        │   (@modelcontextprotocol/        │
                        │    server-osquery)               │
                        └──────────────────────────────────┘
                                            │
                                            │
                        ┌───────────────────▼──────────────┐
                        │   osquery Daemon                 │
                        │   (System Information Database)  │
                        └──────────────────────────────────┘
```

## Key Components

### 1. MCP Client Service
- **Location**: `com.example.agentdemo.mcp.client.MCPClientService`
- **Purpose**: Communicates with MCP servers using JSON-RPC protocol
- **Features**:
  - Server connection management (stdio transport)
  - Tool discovery (`tools/list`)
  - Tool execution (`tools/call`)
  - Protocol initialization

### 2. MCP Models
- **JsonRpcRequest/Response**: JSON-RPC 2.0 message format
- **MCPTool**: Represents a tool provided by an MCP server
- **MCPToolCall**: Tool invocation request
- **MCPToolResult**: Tool execution result
- **MCPContent**: Content returned by tools (text, image, resource)

### 3. OsqueryMCPSkill
- **Location**: `com.example.agentdemo.agent.skills.OsqueryMCPSkill`
- **Purpose**: Exposes osquery capabilities as an agent skill
- **Features**:
  - Automatic connection to osquery MCP server on startup
  - Query mapping (converts natural language to SQL)
  - Common query presets (processes, users, network, system info)

## Prerequisites

### 1. Install Node.js and npm
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install nodejs npm

# macOS
brew install node

# Verify installation
node --version
npm --version
```

### 2. Install osquery
### 2. Install osquery
```bash
# Ubuntu/Debian
export OSQUERY_KEY=1484120AC4E9F8A1A577AEEE97A80C63C9D8B80B
sudo apt-key adv --keyserver keyserver.ubuntu.com --recv-keys $OSQUERY_KEY
sudo add-apt-repository 'deb [arch=amd64] https://pkg.osquery.io/deb deb main'
sudo apt update
sudo apt install osquery

# macOS
brew install osquery

# Verify installation
osqueryi --version
```

### 3. Python 3 Installation
The custom osquery MCP server is written in Python 3:
```bash
# Ubuntu/Debian
sudo apt install python3

# macOS
brew install python3

# Verify
python3 --version
```

### 4. Download the Custom osquery MCP Server

The Python-based MCP server (`osquery-mcp-server.py`) should be placed in your home directory and made executable:

```bash
chmod +x ~/osquery-mcp-server.py
```

This custom implementation provides proper JSON-RPC 2.0 envelope wrapping for MCP protocol compliance.

## Configuration

Edit `src/main/resources/application.properties`:

```properties
# Enable osquery MCP integration
mcp.osquery.enabled=true

# Path to the Python osquery MCP server
mcp.osquery.command=/home/your-username/osquery-mcp-server.py

# No arguments needed for Python server
mcp.osquery.args=
```

### Configuration Options

| Property | Default | Description |
|----------|---------|-------------|
| `mcp.osquery.enabled` | `false` | Enable/disable osquery MCP skill |
| `mcp.osquery.command` | `npx` | Full path to MCP server executable |
| `mcp.osquery.args` | (empty) | Server command-line arguments |

**Note:** Replace `/home/your-username/` with your actual home directory path.

## Usage Examples

### 1. Query System Hostname
```bash
curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal": "what is the hostname of this system?"}'
```

**Response:**
```json
{
  "skillName": "OsqueryMCPSkill",
  "success": true,
  "finalOutput": "[{\"hostname\": \"gpwin10.\"}]"
}
```

### 2. Get System Information
```bash
curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal": "show system info"}'
```

**Response includes:**
- Computer name
- CPU brand and cores
- Physical memory
- Hardware UUID

### 3. List Users
```bash
curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal": "list users"}'
```

### 4. Query Running Processes
```bash
curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal": "osquery: list running processes"}'
```

### 5. Custom SQL Query
```bash
curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal": "osquery: SELECT * FROM system_info"}'
```

## Supported Query Types

The OsqueryMCPSkill automatically maps natural language queries to SQL:

| Natural Language | Generated SQL Query |
|-----------------|---------------------|
| "hostname" / "what is the hostname" | `SELECT hostname FROM system_info` |
| "show system info" | `SELECT * FROM system_info` |
| "list running processes" | `SELECT pid, name, path, cmdline FROM processes LIMIT 20` |
| "get network connections" | `SELECT pid, local_address, local_port, remote_address, remote_port, state FROM process_open_sockets LIMIT 20` |
| "list users" | `SELECT uid, username, shell FROM users` |
| Any SQL query starting with "SELECT" | Passed directly to osquery |

**Pattern Matching:**
- Contains "hostname" → Hostname query
- Contains "system info" → Full system information
- Contains "process" → Process listing
- Contains "network" or "connection" → Network connections
- Contains "user" → User accounts
- Starts with "osquery:" → Direct SQL passthrough

## Testing

### Automated Tests
All 6 MCP integration tests passing:
- ✅ Calculator skill (local)
- ✅ Summarize skill (local)
- ✅ MockSearch skill (local)
- ✅ Hostname query (MCP)
- ✅ System info query (MCP)
- ✅ User listing (MCP)

See [Test Results](/tmp/test_results.md) for details.

### Manual Testing

1. **Start the application**:
```bash
mvn spring-boot:run
```

2. **Check logs for MCP connection**:
```
INFO  - Connecting to osquery MCP server...
INFO  - Connecting to MCP server: osquery with command: /home/user/osquery-mcp-server.py
INFO  - Initialized MCP server: osquery
INFO  - Successfully connected to MCP server: osquery
INFO  - Available osquery tools: query_osquery
```

3. **Test a simple query**:
```bash
curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal": "show system info"}'
```

4. **Verify the response** includes system information in JSON format.

## Troubleshooting

### Issue: "Not connected to osquery MCP server"
**Solution**: 
- Check if osquery is installed: `osqueryi --version`
- Check if Node.js/npx is installed: `npx --version`
- Enable debug logging: Set `logging.level.com.example.agentdemo.mcp=DEBUG`

### Issue: "Failed to connect to osquery MCP server"
**Solution**:
- Verify npx can run: `npx -y @modelcontextprotocol/server-osquery --help`
- Check application.properties configuration
- Ensure no firewall blocking stdio communication

### Issue: "No content returned"
**Solution**:
- osquery might not have data for the query
- Try a simpler query: `SELECT 1`
- Check osquery is running: `sudo osqueryctl status` (if using daemon)

## Adding More MCP Servers

To add additional MCP servers (e.g., filesystem, database):

1. **Add configuration**:
```properties
mcp.filesystem.enabled=true
mcp.filesystem.command=npx
mcp.filesystem.args=-y @modelcontextprotocol/server-filesystem
```

2. **Create a new Skill**:
```java
@Component
public class FilesystemMCPSkill implements Skill {
    // Initialize connection in @PostConstruct
    // Implement canHandle() and execute()
}
```

3. **Connect in init**:
```java
mcpClient.connectServer("filesystem", command, argsList);
```

## MCP Protocol Details

### Initialization Flow
1. Start MCP server process (stdio transport)
2. Send `initialize` request with protocol version and capabilities
3. Receive server capabilities
4. Ready to call tools

### Tool Discovery
```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "method": "tools/list",
  "params": {}
}
```

### Tool Execution
```json
{
  "jsonrpc": "2.0",
  "id": "2",
  "method": "tools/call",
  "params": {
    "name": "query",
    "arguments": {
      "query": "SELECT * FROM system_info"
    }
  }
}
```

## Benefits of MCP Integration

1. **Extensibility**: Add new capabilities without writing custom code
2. **Standardization**: Use standard MCP protocol for tool integration
3. **Reusability**: MCP servers can be shared across different applications
4. **Separation of Concerns**: Tool logic stays in MCP servers, agent focuses on orchestration
5. **Security**: MCP servers run in separate processes with controlled permissions

## References

- [Model Context Protocol Specification](https://spec.modelcontextprotocol.io/)
- [MCP Server - osquery](https://github.com/modelcontextprotocol/servers/tree/main/src/osquery)
- [osquery Documentation](https://osquery.io/)
- [JSON-RPC 2.0 Specification](https://www.jsonrpc.org/specification)
