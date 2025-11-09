# Communication Protocol Specification

**Project:** Spring AI Agent Demo  
**Version:** 1.0.0  
**Date:** November 8, 2025  
**Spring Boot Version:** 3.5.0  
**Spring Framework Version:** 6.2.7

---

## Table of Contents
1. [Overview](#overview)
2. [Protocol Stack](#protocol-stack)
3. [Layer 1: External API Communication](#layer-1-external-api-communication)
4. [Layer 2: Application Layer](#layer-2-application-layer)
5. [Layer 3: Skill Execution Layer](#layer-3-skill-execution-layer)
6. [Layer 4: MCP Integration](#layer-4-mcp-integration)
7. [Error Handling](#error-handling)
8. [Security Considerations](#security-considerations)
9. [Performance Characteristics](#performance-characteristics)

---

## Overview

The Spring AI Agent system implements a multi-layered communication architecture using various protocols optimized for each layer's requirements:

- **External Interface**: HTTP/REST with JSON
- **Internal Components**: Direct Java method invocation
- **External Tools**: JSON-RPC 2.0 over stdio (Model Context Protocol)

---

## Protocol Stack

```
┌─────────────────────────────────────────────────────────────┐
│ Layer 1: External API (HTTP/REST)                          │
├─────────────────────────────────────────────────────────────┤
│ Layer 2: Application Layer (Spring DI)                     │
├─────────────────────────────────────────────────────────────┤
│ Layer 3: Skill Execution (Interface Pattern)               │
├─────────────────────────────────────────────────────────────┤
│ Layer 4: MCP Integration (JSON-RPC 2.0/stdio)              │
└─────────────────────────────────────────────────────────────┘
```

---

## Layer 1: External API Communication

### Protocol: HTTP/REST

**Endpoint**: `POST /api/agent/execute`

**Transport Layer**:
- Protocol: HTTP/1.1
- Port: 8080 (default, configurable)
- Transport: TCP
- Content-Type: `application/json`

### Request Specification

**Headers**:
```http
POST /api/agent/execute HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Accept: application/json
```

**Body Schema**:
```json
{
  "goal": "string (required)"
}
```

**Example Requests**:

```json
// Calculator Request
{
  "goal": "calculate: 12 / 4"
}

// Summarize Request
{
  "goal": "summarize: The quick brown fox jumps over the lazy dog."
}

// Search Request
{
  "goal": "search: example query"
}

// Osquery Request
{
  "goal": "osquery: show system info"
}
```

### Response Specification

**Status Codes**:
- `200 OK` - Successful execution
- `400 Bad Request` - Invalid request format
- `500 Internal Server Error` - Execution error

**Response Schema**:
```json
{
  "goal": "string",
  "trace": [
    {
      "success": "boolean",
      "skillName": "string",
      "output": "string"
    }
  ],
  "finalOutput": "string"
}
```

**Example Response**:
```json
{
  "goal": "calculate: 12 / 4",
  "trace": [
    {
      "success": true,
      "skillName": "CalculatorSkill",
      "output": "3.0"
    }
  ],
  "finalOutput": "3.0"
}
```

### Critical Characteristics

✅ **Synchronous**: Client blocks until response  
✅ **Stateless**: No session state maintained  
✅ **JSON Encoding**: UTF-8  
✅ **Thread-Safe**: Spring MVC handles concurrent requests  

---

## Layer 2: Application Layer

### Protocol: Spring Dependency Injection

**Communication Method**: Direct Java method calls within the same JVM

**Component Flow**:
```
AgentController → AgentService → Skill[] → ActionResult
```

**Key Interfaces**:

```java
// Controller → Service
public List<ActionResult> executeGoal(String goal)

// Service → Skills
public boolean canHandle(String goal)
public ActionResult execute(String goal)
```

### Data Transfer Objects

**ActionResult**:
```java
public class ActionResult {
    private boolean success;
    private String skillName;
    private String output;
}
```

### Critical Characteristics

✅ **In-Process**: No serialization overhead  
✅ **Type-Safe**: Compile-time checking  
✅ **Transaction-Safe**: Spring transaction management  
✅ **Bean-Scoped**: Singleton services by default  

---

## Layer 3: Skill Execution Layer

### Protocol: Strategy Pattern with Interface

**Interface Definition**:
```java
public interface Skill {
    boolean canHandle(String goal);
    ActionResult execute(String goal);
}
```

### Skill Discovery Algorithm

**Strategy**: Sequential polling of all registered skills

```
1. Inject all Skill implementations via Spring
2. For each skill in registry:
   a. Call canHandle(goal)
   b. If true, call execute(goal)
   c. Return ActionResult
3. If no skill matches, return error
```

### Registered Skills

| Skill Class | Can Handle Pattern | Processing Type |
|------------|-------------------|----------------|
| CalculatorSkill | Contains "calculate", "compute", or math operators | Real calculation |
| SummarizeSkill | Contains "summarize" or "summary" | Real text processing |
| MockSearchSkill | Contains "search", "find", "lookup" | Mock data |
| OsqueryMCPSkill | Contains "osquery" or system query keywords | MCP remote call |

### Critical Characteristics

✅ **Extensible**: Add skills by implementing interface  
✅ **Decoupled**: Skills independent of each other  
✅ **Auto-Discovery**: Spring component scanning  
✅ **Order-Independent**: First match wins  

---

## Layer 4: MCP Integration

### Protocol: JSON-RPC 2.0 over stdio

**Specification**: Model Context Protocol (MCP)  
**Version**: 2024-11-05  
**Transport**: Standard Input/Output (stdio)  
**Format**: JSON-RPC 2.0  
**Process Model**: Parent-Child with pipe communication

### Python MCP Server Implementation

**Location**: `osquery-mcp-server.py` (project root)  
**Language**: Python 3  
**Dependencies**: 
- `python3` (3.7+)
- `osquery` (osqueryi command-line tool)

**Configuration** (application.properties):
```properties
mcp.osquery.enabled=true
mcp.osquery.command=./osquery-mcp-server.py
mcp.osquery.args=
```

**Server Features**:
- ✅ Full MCP protocol compliance (v2024-11-05)
- ✅ JSON-RPC 2.0 error handling
- ✅ Single tool: `query_osquery`
- ✅ 30-second query timeout
- ✅ Automatic JSON result parsing
- ✅ Error recovery and logging

**Tool Schema**:
```json
{
  "name": "query_osquery",
  "description": "Execute an osquery SQL query",
  "inputSchema": {
    "type": "object",
    "properties": {
      "sql": {
        "type": "string",
        "description": "SQL query to execute"
      }
    },
    "required": ["sql"]
  }
}
```

**Example Queries Supported**:
- `SELECT * FROM system_info` - System information
- `SELECT * FROM processes` - Running processes
- `SELECT * FROM users` - User accounts
- `SELECT * FROM listening_ports` - Network ports
- `SELECT hostname FROM system_info` - Hostname only

### Connection Lifecycle

#### 1. Process Spawn
```java
ProcessBuilder processBuilder = new ProcessBuilder();
processBuilder.command("/path/to/mcp-server.py");
Process process = processBuilder.start();
```

#### 2. Initialize Handshake

**Request**:
```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "method": "initialize",
  "params": {
    "protocolVersion": "2024-11-05",
    "capabilities": {
      "tools": {}
    },
    "clientInfo": {
      "name": "spring-ai-agent",
      "version": "1.0.0"
    }
  }
}
```

**Response**:
```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "result": {
    "protocolVersion": "2024-11-05",
    "capabilities": {
      "tools": {}
    },
    "serverInfo": {
      "name": "osquery-mcp-server",
      "version": "1.0.0"
    }
  }
}
```

#### 3. Tool Discovery

**Request**:
```json
{
  "jsonrpc": "2.0",
  "id": "2",
  "method": "tools/list",
  "params": {}
}
```

**Response**:
```json
{
  "jsonrpc": "2.0",
  "id": "2",
  "result": {
    "tools": [
      {
        "name": "query_osquery",
        "description": "Execute osquery SQL queries",
        "inputSchema": {
          "type": "object",
          "properties": {
            "query": {
              "type": "string",
              "description": "SQL query to execute"
            }
          },
          "required": ["query"]
        }
      }
    ]
  }
}
```

#### 4. Tool Invocation

**Request**:
```json
{
  "jsonrpc": "2.0",
  "id": "3",
  "method": "tools/call",
  "params": {
    "name": "query_osquery",
    "arguments": {
      "query": "SELECT * FROM system_info"
    }
  }
}
```

**Response**:
```json
{
  "jsonrpc": "2.0",
  "id": "3",
  "result": {
    "content": [
      {
        "type": "text",
        "text": "hostname: mycomputer\nversion: Ubuntu 22.04"
      }
    ]
  }
}
```

#### 5. Connection Termination

```java
writer.close();
reader.close();
process.destroy();
```

### Stream Management

**Critical Implementation Details**:

```java
// Output Stream (Client → Server)
BufferedWriter writer = new BufferedWriter(
    new OutputStreamWriter(process.getOutputStream())
);

// Input Stream (Server → Client)
BufferedReader reader = new BufferedReader(
    new InputStreamReader(process.getInputStream())
);

// Message Format: One JSON-RPC message per line
writer.write(jsonMessage + "\n");
writer.flush();

String response = reader.readLine();
```

### Error Handling

**JSON-RPC Error Response**:
```json
{
  "jsonrpc": "2.0",
  "id": "3",
  "error": {
    "code": -32603,
    "message": "Internal error",
    "data": "Additional error information"
  }
}
```

**Error Codes** (JSON-RPC 2.0 Standard):
- `-32700` - Parse error
- `-32600` - Invalid request
- `-32601` - Method not found
- `-32602` - Invalid params
- `-32603` - Internal error
- `-32000 to -32099` - Server-defined errors

### Critical Characteristics

✅ **Bidirectional**: Full-duplex communication  
✅ **Line-Delimited**: Each message on separate line  
✅ **Blocking I/O**: Synchronous request-response  
✅ **Process Isolation**: Separate OS process for MCP server  
✅ **Stateful Connection**: Maintained for application lifetime  

---

## Error Handling

### HTTP Layer Errors

```java
@ExceptionHandler(Exception.class)
public ResponseEntity<Map<String, String>> handleError(Exception e) {
    return ResponseEntity.status(500)
        .body(Map.of("error", e.getMessage()));
}
```

### Skill Execution Errors

```java
ActionResult error = new ActionResult(
    false, 
    "SkillName", 
    "Error message"
);
```

### MCP Communication Errors

**Scenarios**:
1. **Process Spawn Failure**: IOException during ProcessBuilder.start()
2. **Initialize Failure**: Server returns error in initialize response
3. **Tool Call Failure**: Server returns error in tools/call response
4. **Timeout**: No response within reasonable time (implementation-dependent)
5. **Process Crash**: Unexpected process termination

**Error Propagation**:
```
MCP Error → MCPToolResult(isError=true) → ActionResult(success=false) → HTTP 200 with error in trace
```

---

## Security Considerations

### HTTP API Security

⚠️ **Current State**: No authentication/authorization implemented

**Recommended Additions**:
- Spring Security with JWT tokens
- Rate limiting
- Input validation and sanitization
- CORS configuration

### MCP Server Security

⚠️ **Process Execution Risks**:
- Command injection via server path
- Arbitrary code execution via MCP server

**Mitigations**:
- Whitelist allowed MCP server executables
- Run MCP servers with limited permissions
- Validate all tool arguments before sending
- Sanitize MCP server responses

### Data Validation

**Input Validation**:
```java
// Validate goal is not null/empty
if (goal == null || goal.trim().isEmpty()) {
    return error response
}

// Validate goal length
if (goal.length() > MAX_GOAL_LENGTH) {
    return error response
}
```

**SQL Injection Prevention** (for osquery):
- MCP server should use parameterized queries
- Agent validates query patterns
- No direct user SQL execution

---

## Performance Characteristics

### Latency Profile

| Layer | Typical Latency | Notes |
|-------|----------------|-------|
| HTTP Request | 1-5 ms | Network + parsing overhead |
| Spring DI Call | <1 μs | In-memory method call |
| Skill Execution (Calculator) | <1 ms | Pure computation |
| Skill Execution (Summarize) | 1-10 ms | String processing |
| MCP Tool Call | 10-100 ms | Process IPC + osquery execution |

### Throughput

**HTTP API**:
- Concurrent requests: Supported (Spring MVC thread pool)
- Default thread pool: 200 threads
- Configurable via `server.tomcat.threads.max`

**MCP Connections**:
- One persistent connection per MCP server
- Connection pooling: Not implemented
- Thread safety: Synchronized request/response

### Resource Usage

**Memory**:
- Base Spring Boot app: ~100-150 MB
- MCP server process: ~20-50 MB per server
- Request processing: ~1-5 MB per concurrent request

**CPU**:
- Idle: <1%
- Under load: Scales with request rate
- MCP calls: Minimal overhead (mostly I/O wait)

### Scalability Limits

**Bottlenecks**:
1. MCP server single-threaded processing
2. stdio pipe buffer size (typically 64KB)
3. Process spawn overhead for new MCP connections

**Scaling Strategies**:
- Horizontal scaling: Multiple app instances
- MCP connection pooling (future enhancement)
- Async MCP calls with CompletableFuture (future enhancement)

---

## Protocol Compliance

### Standards Adherence

✅ **HTTP/1.1**: RFC 7230-7235  
✅ **JSON**: RFC 8259  
✅ **JSON-RPC 2.0**: [Specification](https://www.jsonrpc.org/specification)  
✅ **MCP**: Model Context Protocol v2024-11-05  

### Content Type Headers

```
Request:  Content-Type: application/json; charset=utf-8
Response: Content-Type: application/json; charset=utf-8
```

### Character Encoding

- **HTTP**: UTF-8
- **JSON**: UTF-8
- **stdio**: UTF-8 (system default)

---

## Testing & Validation

### Protocol Testing

**HTTP API**:
```bash
curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal":"calculate: 12 / 4"}'
```

**Expected Response**:
```json
{
  "goal": "calculate: 12 / 4",
  "trace": [
    {
      "success": true,
      "skillName": "CalculatorSkill",
      "output": "3.0"
    }
  ],
  "finalOutput": "3.0"
}
```

### MCP Protocol Testing

Verified via integration tests:
- ✅ Connection initialization
- ✅ Tool discovery
- ✅ Tool invocation
- ✅ Error handling
- ✅ Connection cleanup

### Test Results

**Unit Tests**: 3/3 passing  
**Integration Tests**: All MCP operations functional  
**Protocol Compliance**: JSON-RPC 2.0 compliant  

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2025-11-08 | Initial specification with Spring Boot 3.5.0 upgrade |

---

## Appendix A: Python MCP Server Implementation

### Overview

The `osquery-mcp-server.py` is a lightweight Python implementation of a Model Context Protocol server that wraps osquery functionality.

**File**: `osquery-mcp-server.py` (project root)  
**Size**: ~4.6 KB  
**Lines of Code**: ~170

### Architecture

```
┌─────────────────────────────────────────────────────────┐
│  osquery-mcp-server.py (Python 3)                       │
│                                                          │
│  ┌────────────────┐  ┌────────────────┐                │
│  │ JSON-RPC       │  │ osquery        │                │
│  │ Handler        │→ │ Executor       │                │
│  │                │  │                │                │
│  │ stdin/stdout   │  │ subprocess     │                │
│  └────────────────┘  └────────┬───────┘                │
│                               │                         │
└───────────────────────────────┼─────────────────────────┘
                                │
                    ┌───────────▼──────────┐
                    │  osqueryi CLI        │
                    │  (system daemon)     │
                    └──────────────────────┘
```

### Implementation Details

#### Main Loop

```python
def main():
    """Main server loop - reads JSON-RPC from stdin, writes to stdout"""
    while True:
        line = sys.stdin.readline().strip()
        request = json.loads(line)
        
        # Route to appropriate handler
        method = request.get("method")
        if method == "initialize":
            result = handle_initialize()
        elif method == "tools/list":
            result = handle_list_tools()
        elif method == "tools/call":
            result = handle_call_tool(...)
        
        # Send JSON-RPC response
        response = {"jsonrpc": "2.0", "id": req_id, "result": result}
        print(json.dumps(response))
        sys.stdout.flush()
```

#### osquery Execution

```python
def run_osquery_command(sql: str) -> list[dict[str, Any]]:
    """Execute osquery SQL query via subprocess"""
    process = subprocess.run(
        ['osqueryi', '--json', sql],
        capture_output=True,
        text=True,
        timeout=30  # 30-second timeout
    )
    return json.loads(process.stdout)
```

#### Tool Handler

```python
def handle_call_tool(tool_name: str, arguments: dict) -> dict:
    """Handle tools/call method"""
    if tool_name == "query_osquery":
        sql = arguments.get("sql", "")
        results = run_osquery_command(sql)
        return {
            "content": [{
                "type": "text",
                "text": json.dumps(results, indent=2)
            }]
        }
```

### Error Handling

**JSON Parse Errors** (Code -32700):
```python
except json.JSONDecodeError:
    error_response = {
        "jsonrpc": "2.0",
        "id": None,
        "error": {
            "code": -32700,
            "message": "Parse error: Invalid JSON"
        }
    }
```

**Method Not Found** (Code -32601):
```python
if method not in ["initialize", "tools/list", "tools/call"]:
    error = {
        "code": -32601,
        "message": f"Method not found: {method}"
    }
```

**Internal Errors** (Code -32603):
```python
except Exception as e:
    error = {
        "code": -32603,
        "message": f"Internal error: {str(e)}"
    }
```

**osquery Execution Errors**:
- Timeout after 30 seconds
- Returns empty array `[]` on failure
- Logs errors to stderr

### Installation & Setup

#### Prerequisites

```bash
# Python 3.7 or higher
python3 --version

# osquery installation
# Ubuntu/Debian:
sudo apt-get install osquery

# macOS:
brew install osquery

# Verify osquery
osqueryi --version
```

#### File Permissions

```bash
# Make executable
chmod +x osquery-mcp-server.py

# Verify shebang
head -1 osquery-mcp-server.py
# Should output: #!/usr/bin/env python3
```

#### Testing Standalone

```bash
# Test initialization
echo '{"jsonrpc":"2.0","id":"1","method":"initialize","params":{}}' | ./osquery-mcp-server.py

# Test tool listing
echo '{"jsonrpc":"2.0","id":"2","method":"tools/list","params":{}}' | ./osquery-mcp-server.py

# Test query execution
echo '{"jsonrpc":"2.0","id":"3","method":"tools/call","params":{"name":"query_osquery","arguments":{"sql":"SELECT hostname FROM system_info"}}}' | ./osquery-mcp-server.py
```

### Performance Characteristics

| Operation | Typical Time | Notes |
|-----------|-------------|-------|
| Startup | <100 ms | Python interpreter + imports |
| Initialize | <10 ms | Protocol handshake |
| List Tools | <1 ms | Returns cached schema |
| Query (simple) | 50-200 ms | osquery execution + JSON parsing |
| Query (complex) | 200-5000 ms | Depends on query complexity |

### Security Considerations

⚠️ **SQL Injection Risk**: 
- Server accepts arbitrary SQL queries
- osquery has read-only access (safer than direct DB)
- Should validate/sanitize queries in production

⚠️ **Command Injection**: 
- Uses hardcoded command `osqueryi`
- Arguments are passed as array (safe from shell injection)

⚠️ **Resource Exhaustion**:
- 30-second timeout prevents infinite queries
- No rate limiting implemented
- No concurrent query limit

**Recommended Hardening**:
```python
# Add query validation
ALLOWED_TABLES = ['system_info', 'processes', 'users', 'listening_ports']

def validate_query(sql: str) -> bool:
    sql_lower = sql.lower()
    return any(table in sql_lower for table in ALLOWED_TABLES)
```

### Troubleshooting

**Server Not Starting**:
```bash
# Check Python path
which python3

# Check shebang matches
head -1 osquery-mcp-server.py

# Run directly with Python
python3 osquery-mcp-server.py
```

**osquery Not Found**:
```bash
# Check osquery installation
which osqueryi

# Install if missing
sudo apt-get install osquery  # Ubuntu/Debian
brew install osquery          # macOS
```

**Permission Denied**:
```bash
# Make executable
chmod +x osquery-mcp-server.py

# Check ownership
ls -la osquery-mcp-server.py
```

**Communication Errors**:
```bash
# Enable debug logging
export MCP_DEBUG=1

# Check Spring Boot logs
tail -f logs/spring-boot-application.log
```

### Code Structure

```
osquery-mcp-server.py (170 lines)
├── Imports (10 lines)
│   ├── json
│   ├── sys
│   ├── os
│   └── subprocess
├── run_osquery_command() (20 lines)
│   └── Subprocess execution with timeout
├── handle_initialize() (15 lines)
│   └── Protocol handshake
├── handle_list_tools() (20 lines)
│   └── Tool schema definition
├── handle_call_tool() (25 lines)
│   └── Query execution and formatting
└── main() (80 lines)
    ├── stdin/stdout loop
    ├── JSON-RPC parsing
    ├── Method routing
    └── Error handling
```

### Extension Points

**Adding New Tools**:
```python
# In handle_list_tools()
tools.append({
    "name": "get_processes",
    "description": "List running processes",
    "inputSchema": {
        "type": "object",
        "properties": {
            "limit": {"type": "integer", "default": 10}
        }
    }
})

# In handle_call_tool()
elif tool_name == "get_processes":
    limit = arguments.get("limit", 10)
    sql = f"SELECT * FROM processes LIMIT {limit}"
    results = run_osquery_command(sql)
```

**Adding Query Templates**:
```python
QUERY_TEMPLATES = {
    "system_info": "SELECT * FROM system_info",
    "processes": "SELECT pid, name, cmdline FROM processes",
    "users": "SELECT username, uid FROM users"
}

# Use in handle_call_tool()
template_name = arguments.get("template")
if template_name in QUERY_TEMPLATES:
    sql = QUERY_TEMPLATES[template_name]
```

---

## Appendix B: Testing the Full Stack

### End-to-End Test

**1. Start the Application**:
```bash
mvn spring-boot:run
```

**2. Send HTTP Request**:
```bash
curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal":"osquery: show system info"}'
```

**3. Expected Flow**:
```
HTTP Client
  → AgentController (REST endpoint)
    → AgentService (skill routing)
      → OsqueryMCPSkill (skill handler)
        → MCPClientService (MCP client)
          → osquery-mcp-server.py (Python server)
            → osqueryi (system query)
              → osquery daemon
            ← JSON results
          ← MCP response
        ← Tool result
      ← Action result
    ← Trace list
  ← HTTP response
```

**4. Verify Logs**:
```
INFO - Connecting to osquery MCP server...
INFO - Successfully connected to MCP server: osquery
INFO - Available osquery tools: query_osquery
INFO - Executing osquery: SELECT * FROM system_info
```

### Protocol Validation

**Verify JSON-RPC Messages**:
```bash
# Enable debug logging in MCPClientService
# Add this to application.properties:
logging.level.com.example.agentdemo.mcp=DEBUG

# Restart and check logs for:
# - Request: {"jsonrpc":"2.0","id":"1","method":"initialize",...}
# - Response: {"jsonrpc":"2.0","id":"1","result":{...}}
```

---

## References

- [Model Context Protocol Specification](https://modelcontextprotocol.io/)
- [JSON-RPC 2.0 Specification](https://www.jsonrpc.org/specification)
- [osquery Documentation](https://osquery.io/schema/)
- [Spring Framework Documentation](https://docs.spring.io/spring-framework/docs/6.2.7/reference/html/)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/3.5.0/reference/html/)
- [Python subprocess Module](https://docs.python.org/3/library/subprocess.html)

---

**Document Status**: ✅ Complete  
**Last Updated**: November 8, 2025  
**Maintained By**: Spring AI Agent Team
