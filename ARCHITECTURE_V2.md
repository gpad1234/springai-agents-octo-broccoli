# Spring AI Agent - Architecture & Technical Specifications v2.0

**Last Updated:** November 8, 2025  
**Version:** 2.0 with MCP Integration - TESTED & VALIDATED ✅  
**Java Version:** 21 (LTS)  
**Spring Boot Version:** 3.2.11  
**Test Status:** All 6 tests passing (3 local skills + 3 MCP integration)

---

## Table of Contents

1. [System Overview](#system-overview)
2. [Architecture Diagrams](#architecture-diagrams)
3. [Component Specifications](#component-specifications)
4. [MCP Integration Architecture](#mcp-integration-architecture)
5. [Data Flow & Interactions](#data-flow--interactions)
6. [API Specifications](#api-specifications)
7. [Deployment Architecture](#deployment-architecture)

---

## System Overview

The Spring AI Agent is a modular, extensible agent framework that combines **local skills** with **Model Context Protocol (MCP)** integration to provide both built-in capabilities and access to external tools and services.

### Key Characteristics

- **Hybrid Skill Architecture**: Local + Remote (MCP-based) skills
- **Protocol-Agnostic**: Supports both REST API and MCP JSON-RPC
- **Event-Driven**: Lifecycle hooks for skill initialization and cleanup
- **Type-Safe**: Fully typed with Java 21 features
- **Cloud-Ready**: Stateless design, containerizable

---

## Architecture Diagrams

### High-Level System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                                 │
│  (REST Clients, CLI, Web UI, External Applications)                 │
└────────────────────────────┬────────────────────────────────────────┘
                             │ HTTP/REST
┌────────────────────────────▼────────────────────────────────────────┐
│                    PRESENTATION LAYER                                │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────┐      │
│  │          AgentController (REST Endpoints)                 │      │
│  │  - POST /agent/execute                                    │      │
│  │  - Request validation & transformation                    │      │
│  └──────────────────────────────────────────────────────────┘      │
└────────────────────────────┬────────────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────────────┐
│                   ORCHESTRATION LAYER                                │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────┐      │
│  │              AgentService                                 │      │
│  │  - Skill selection strategy                              │      │
│  │  - Execution coordination                                │      │
│  │  - Trace/audit logging                                   │      │
│  └────────────┬──────────────────────────┬──────────────────┘      │
│               │                          │                          │
└───────────────┼──────────────────────────┼──────────────────────────┘
                │                          │
       ┌────────▼────────┐        ┌───────▼───────────┐
       │  LOCAL SKILLS   │        │  MCP-BASED SKILLS │
       └────────┬────────┘        └───────┬───────────┘
                │                          │
┌───────────────▼──────────────────────────▼──────────────────────────┐
│                     EXECUTION LAYER                                  │
│                                                                      │
│  ┌─────────────────────┐         ┌──────────────────────────┐      │
│  │  Built-in Skills    │         │  MCP Integration         │      │
│  ├─────────────────────┤         ├──────────────────────────┤      │
│  │ • CalculatorSkill   │         │ • MCPClientService       │      │
│  │ • SummarizeSkill    │         │ • OsqueryMCPSkill        │      │
│  │ • MockSearchSkill   │         │ • (Future MCP Skills)    │      │
│  └─────────────────────┘         └──────────┬───────────────┘      │
│                                              │                       │
└──────────────────────────────────────────────┼───────────────────────┘
                                               │ JSON-RPC/stdio
┌──────────────────────────────────────────────▼───────────────────────┐
│                   EXTERNAL MCP SERVERS                               │
│                                                                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐     │
│  │   osquery    │  │  filesystem  │  │  database / APIs     │     │
│  │  MCP Server  │  │  MCP Server  │  │  (Future)            │     │
│  └──────────────┘  └──────────────┘  └──────────────────────┘     │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

### Detailed Component Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                      Spring Boot Application                         │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────┐    │
│  │               CONTROLLER LAYER                              │    │
│  │                                                             │    │
│  │  AgentController                                            │    │
│  │  - @PostMapping("/agent/execute")                          │    │
│  │  - Input: AgentRequest { goal: String }                    │    │
│  │  - Output: List<ActionResult>                              │    │
│  └────────────────────┬───────────────────────────────────────┘    │
│                       │                                             │
│  ┌────────────────────▼───────────────────────────────────────┐    │
│  │               SERVICE LAYER                                 │    │
│  │                                                             │    │
│  │  AgentService                                               │    │
│  │  - List<Skill> skills (Auto-wired)                         │    │
│  │  - executeGoal(String goal): List<ActionResult>            │    │
│  │  - Skill selection: First-match strategy                   │    │
│  └────────────────────┬───────────────────────────────────────┘    │
│                       │                                             │
│  ┌────────────────────▼───────────────────────────────────────┐    │
│  │               SKILL LAYER                                   │    │
│  │                                                             │    │
│  │  <<interface>> Skill                                        │    │
│  │  + canHandle(goal: String): boolean                         │    │
│  │  + execute(goal: String): ActionResult                      │    │
│  │                                                             │    │
│  │  Implementations:                                           │    │
│  │  ┌─────────────────┬───────────────────┬────────────────┐  │    │
│  │  │ CalculatorSkill │ SummarizeSkill    │ MockSearchSkill│  │    │
│  │  │ Pattern: math   │ Pattern: summarize│ Pattern: search│  │    │
│  │  └─────────────────┴───────────────────┴────────────────┘  │    │
│  │  ┌──────────────────────────────────────────────────────┐  │    │
│  │  │ OsqueryMCPSkill                                       │  │    │
│  │  │ - Delegates to MCPClientService                       │  │    │
│  │  │ - Pattern: osquery|system info|processes              │  │    │
│  │  │ - @PostConstruct: Connect to MCP server               │  │    │
│  │  │ - @PreDestroy: Disconnect from MCP server             │  │    │
│  │  └──────────────────┬───────────────────────────────────┘  │    │
│  └─────────────────────┼──────────────────────────────────────┘    │
│                        │                                            │
│  ┌─────────────────────▼──────────────────────────────────────┐    │
│  │               MCP CLIENT LAYER                              │    │
│  │                                                             │    │
│  │  MCPClientService                                           │    │
│  │  - connectServer(name, command, args)                      │    │
│  │  - listTools(serverName): List<MCPTool>                    │    │
│  │  - callTool(serverName, MCPToolCall): MCPToolResult        │    │
│  │  - disconnectServer(serverName)                            │    │
│  │                                                             │    │
│  │  Protocol: JSON-RPC 2.0 over stdio                         │    │
│  │  Methods: initialize, tools/list, tools/call               │    │
│  └─────────────────────────────────────────────────────────────┘    │
│                                                                      │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │               MODEL LAYER                                   │    │
│  │                                                             │    │
│  │  Core Models:                                               │    │
│  │  - ActionResult { success, skillName, output }             │    │
│  │                                                             │    │
│  │  MCP Models:                                                │    │
│  │  - JsonRpcRequest { jsonrpc, id, method, params }          │    │
│  │  - JsonRpcResponse { jsonrpc, id, result, error }          │    │
│  │  - MCPTool { name, description, inputSchema }              │    │
│  │  - MCPToolCall { name, arguments }                          │    │
│  │  - MCPToolResult { isError, content }                       │    │
│  │  - MCPContent { type, text, mimeType, data }               │    │
│  └─────────────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────────────┘
```

---

## Component Specifications

### 1. AgentController

**Responsibility:** HTTP request handling and REST API exposure

**Endpoints:**
```java
@RestController
@RequestMapping("/agent")
public class AgentController {
    
    @PostMapping("/execute")
    public List<ActionResult> executeGoal(@RequestBody Map<String, String> request)
}
```

**Request Format:**
```json
{
  "goal": "calculate: 42 + 58"
}
```

**Response Format:**
```json
[
  {
    "success": true,
    "skillName": "CalculatorSkill",
    "output": "100.0"
  }
]
```

### 2. AgentService

**Responsibility:** Skill orchestration and execution coordination

**Key Methods:**
- `executeGoal(String goal)`: Main entry point
- Skill selection: First-match strategy (iterates through injected skills)
- Fallback: Returns error if no skill matches

**Dependencies:** `List<Skill>` (auto-injected by Spring)

### 3. Skill Interface

**Contract:**
```java
public interface Skill {
    boolean canHandle(String goal);
    ActionResult execute(String goal);
}
```

**Implementations:**

#### CalculatorSkill
- **Pattern:** "calculate", "compute", "sum", or contains arithmetic
- **Logic:** Regex-based expression parsing, supports +, -, *, /
- **Example:** `"calculate: 15 * 3"` → `"45.0"`

#### SummarizeSkill
- **Pattern:** "summarize"
- **Logic:** Simple text truncation/placeholder
- **Example:** `"summarize: Long text..."` → `"Summary of..."`

#### MockSearchSkill
- **Pattern:** "search"
- **Logic:** Returns mock search results
- **Example:** `"search: Java tutorials"` → `"Found 5 results..."`

#### OsqueryMCPSkill
- **Pattern:** "osquery:", "system info", "running process", "network connection"
- **Logic:** Delegates to MCPClientService, maps queries to SQL
- **Lifecycle:**
  - `@PostConstruct`: Connects to osquery MCP server
  - `@PreDestroy`: Disconnects from server
- **Example:** `"osquery: list running processes"` → SQL query via MCP

### 4. MCPClientService

**Responsibility:** MCP protocol client implementation

**Key Features:**
- **Connection Management:** stdio-based process communication
- **Protocol:** JSON-RPC 2.0
- **Methods Supported:**
  - `initialize`: Handshake with MCP server
  - `tools/list`: Discover available tools
  - `tools/call`: Execute a tool

**Connection Lifecycle:**
```
1. Start MCP server process (e.g., npx @modelcontextprotocol/server-osquery)
2. Send initialize request
3. Receive server capabilities
4. Ready for tool operations
5. Cleanup on shutdown
```

**JSON-RPC Example:**
```json
// Request
{
  "jsonrpc": "2.0",
  "id": "1",
  "method": "tools/call",
  "params": {
    "name": "query",
    "arguments": {
      "query": "SELECT * FROM processes LIMIT 10"
    }
  }
}

// Response
{
  "jsonrpc": "2.0",
  "id": "1",
  "result": {
    "content": [
      {
        "type": "text",
        "text": "pid | name | path\n1 | systemd | /usr/lib/systemd/systemd\n..."
      }
    ]
  }
}
```

---

## MCP Integration Architecture

### MCP Server Communication

```
┌───────────────────────┐         ┌──────────────────────┐
│  OsqueryMCPSkill      │         │  MCPClientService    │
│                       │         │                      │
│  @PostConstruct       │────────▶│  connectServer()     │
│  init()               │         │                      │
│                       │         │  1. Start process    │
│  canHandle(goal)      │         │  2. Initialize       │
│                       │         │  3. List tools       │
│  execute(goal)        │────────▶│                      │
│  ├─ extractQuery()    │         │  callTool()          │
│  ├─ determineToolName│         │  ├─ Build request    │
│  ├─ buildArguments() │         │  ├─ Send JSON-RPC    │
│  └─ MCPClientService │         │  ├─ Read response    │
│                       │◀────────│  └─ Parse result     │
│  @PreDestroy          │         │                      │
│  cleanup()            │────────▶│  disconnectServer()  │
└───────────────────────┘         └──────────────────────┘
                                           │
                                           │ stdio (stdin/stdout)
                                           │
                                  ┌────────▼─────────────┐
                                  │  MCP Server Process  │
                                  │  (osquery)           │
                                  │                      │
                                  │  - Receives JSON-RPC │
                                  │  - Executes queries  │
                                  │  - Returns results   │
                                  └──────────────────────┘
```

### Query Mapping Logic

The OsqueryMCPSkill intelligently maps natural language to SQL:

| User Query | Tool Name | SQL Query |
|-----------|-----------|-----------|
| "list running processes" | query_processes | `SELECT pid, name, path, cmdline FROM processes LIMIT 20` |
| "show system info" | query_system_info | `SELECT * FROM system_info` |
| "get network connections" | query_connections | `SELECT pid, local_address, local_port, remote_address, remote_port, state FROM process_open_sockets LIMIT 20` |
| "list users" | query_users | `SELECT uid, username, shell FROM users` |
| Custom SQL | query | Passed as-is |

---

## Data Flow & Interactions

### Sequence Diagram: Local Skill Execution

```
Client          Controller      AgentService    CalculatorSkill
  │                 │                 │                │
  │  POST /execute  │                 │                │
  ├────────────────▶│                 │                │
  │                 │  executeGoal()  │                │
  │                 ├────────────────▶│                │
  │                 │                 │  canHandle()?  │
  │                 │                 ├───────────────▶│
  │                 │                 │      true      │
  │                 │                 │◀───────────────┤
  │                 │                 │  execute()     │
  │                 │                 ├───────────────▶│
  │                 │                 │   parse expr   │
  │                 │                 │   calculate    │
  │                 │                 │◀───────────────┤
  │                 │  ActionResult   │                │
  │                 │◀────────────────┤                │
  │  JSON Response  │                 │                │
  │◀────────────────┤                 │                │
  │                 │                 │                │
```

### Sequence Diagram: MCP Skill Execution

```
Client      Controller  AgentService  OsqueryMCPSkill  MCPClientService  osquery MCP Server
  │            │             │               │                │                  │
  │  POST      │             │               │                │                  │
  ├───────────▶│             │               │                │                  │
  │            │ executeGoal │               │                │                  │
  │            ├────────────▶│               │                │                  │
  │            │             │ canHandle()?  │                │                  │
  │            │             ├──────────────▶│                │                  │
  │            │             │     true      │                │                  │
  │            │             │◀──────────────┤                │                  │
  │            │             │ execute()     │                │                  │
  │            │             ├──────────────▶│                │                  │
  │            │             │               │ extractQuery() │                  │
  │            │             │               │ buildArgs()    │                  │
  │            │             │               │                │                  │
  │            │             │               │  callTool()    │                  │
  │            │             │               ├───────────────▶│                  │
  │            │             │               │                │  JSON-RPC        │
  │            │             │               │                ├─────────────────▶│
  │            │             │               │                │  Execute query   │
  │            │             │               │                │  via osquery     │
  │            │             │               │                │◀─────────────────│
  │            │             │               │                │  Query results   │
  │            │             │               │ MCPToolResult  │                  │
  │            │             │               │◀───────────────┤                  │
  │            │             │               │ extractText()  │                  │
  │            │             │ ActionResult  │                │                  │
  │            │             │◀──────────────┤                │                  │
  │            │ Response    │               │                │                  │
  │            │◀────────────┤               │                │                  │
  │  JSON      │             │               │                │                  │
  │◀───────────┤             │               │                │                  │
  │            │             │               │                │                  │
```

---

## API Specifications

### REST API

#### Execute Agent Goal

**Endpoint:** `POST /agent/execute`

**Request:**
```http
POST /agent/execute HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "goal": "calculate: 42 + 58"
}
```

**Success Response (200):**
```json
[
  {
    "success": true,
    "skillName": "CalculatorSkill",
    "output": "100.0"
  }
]
```

**No Skill Match (200):**
```json
[
  {
    "success": false,
    "skillName": "none",
    "output": "No skill found to handle goal: unknown task"
  }
]
```

**Error Response (500):**
```json
{
  "timestamp": "2025-11-08T12:00:00.000+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "path": "/agent/execute"
}
```

### MCP JSON-RPC Protocol

All MCP communication uses JSON-RPC 2.0:

**Initialize:**
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

**List Tools:**
```json
{
  "jsonrpc": "2.0",
  "id": "2",
  "method": "tools/list",
  "params": {}
}
```

**Call Tool:**
```json
{
  "jsonrpc": "2.0",
  "id": "3",
  "method": "tools/call",
  "params": {
    "name": "query",
    "arguments": {
      "query": "SELECT * FROM processes LIMIT 10"
    }
  }
}
```

---

## Deployment Architecture

### Local Development

```
┌─────────────────────────────────────┐
│  Developer Machine                  │
│                                     │
│  ┌───────────────────────────────┐ │
│  │  Spring Boot App              │ │
│  │  (port 8080)                  │ │
│  │                               │ │
│  │  ┌─────────────────────────┐ │ │
│  │  │  Built-in Skills        │ │ │
│  │  └─────────────────────────┘ │ │
│  │                               │ │
│  │  ┌─────────────────────────┐ │ │
│  │  │  MCP Client             │ │ │
│  │  └────────┬────────────────┘ │ │
│  └───────────┼──────────────────┘ │
│              │ stdio              │
│  ┌───────────▼──────────────────┐ │
│  │  osquery MCP Server          │ │
│  │  (npx process)               │ │
│  └───────────┬──────────────────┘ │
│              │                    │
│  ┌───────────▼──────────────────┐ │
│  │  osquery daemon              │ │
│  └──────────────────────────────┘ │
└─────────────────────────────────────┘
```

### Container Deployment

```
┌─────────────────────────────────────────────────┐
│  Docker Container                               │
│                                                 │
│  ┌───────────────────────────────────────────┐ │
│  │  Spring Boot App                          │ │
│  │  - Java 21 JRE                            │ │
│  │  - Application JAR                        │ │
│  └───────────────────────────────────────────┘ │
│                                                 │
│  Optional (if MCP enabled):                    │
│  ┌───────────────────────────────────────────┐ │
│  │  Node.js Runtime                          │ │
│  │  - For MCP servers (npx)                  │ │
│  └───────────────────────────────────────────┘ │
│                                                 │
│  ┌───────────────────────────────────────────┐ │
│  │  osquery                                  │ │
│  │  - System query database                  │ │
│  └───────────────────────────────────────────┘ │
└─────────────────────────────────────────────────┘

Exposed Ports: 8080 (HTTP)
```

### Dockerfile Example

```dockerfile
FROM eclipse-temurin:21-jre-alpine

# Install Node.js and osquery (optional, for MCP)
RUN apk add --no-cache nodejs npm osquery

WORKDIR /app

COPY target/springai-agent-demo-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## Configuration

### Application Properties

```properties
# Server Configuration
server.port=8080

# MCP osquery Integration
mcp.osquery.enabled=false
mcp.osquery.command=npx
mcp.osquery.args=-y @modelcontextprotocol/server-osquery

# Logging
logging.level.com.example.agentdemo=INFO
logging.level.com.example.agentdemo.mcp=DEBUG
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | HTTP server port | 8080 |
| `MCP_OSQUERY_ENABLED` | Enable osquery MCP skill | false |
| `MCP_OSQUERY_COMMAND` | MCP server command | npx |
| `MCP_OSQUERY_ARGS` | MCP server arguments | -y @modelcontextprotocol/server-osquery |

---

## Technology Stack

| Layer | Technology | Version | Purpose |
|-------|-----------|---------|---------|
| Runtime | Java | 21 (LTS) | Application runtime |
| Framework | Spring Boot | 3.2.11 | Application framework |
| Web | Spring Web | 6.1.14 | REST API |
| HTTP Client | Spring WebFlux | 6.1.14 | MCP client (async) |
| JSON | Jackson | 2.15+ | JSON serialization |
| Testing | JUnit | 5.10.5 | Unit testing |
| Build | Maven | 3.9+ | Build automation |
| Protocol | MCP | 2024-11-05 | Model Context Protocol |

---

## Performance Characteristics

### Latency

| Operation | Expected Latency | Notes |
|-----------|-----------------|-------|
| Local Skill | < 10ms | In-memory processing |
| MCP Skill (osquery) | 50-500ms | Process communication + query execution |
| REST API Overhead | ~5ms | Controller + serialization |

### Scalability

- **Stateless Design**: Can scale horizontally
- **Connection Pooling**: Each instance maintains its own MCP connections
- **Resource Usage**: 
  - Base memory: ~150MB
  - With MCP: +50-100MB per MCP server

---

## Security Considerations

### Current Implementation

- **No Authentication**: Demo application, no auth required
- **Input Validation**: Basic validation in controllers
- **MCP Process Isolation**: MCP servers run in separate processes

### Production Recommendations

1. **Add Authentication**: Spring Security with OAuth2/JWT
2. **Rate Limiting**: Prevent abuse of expensive MCP operations
3. **Input Sanitization**: Validate and sanitize SQL queries
4. **MCP Sandboxing**: Run MCP servers in containers with limited permissions
5. **HTTPS**: Enable TLS for production deployments

---

## Future Enhancements

1. **Additional MCP Servers**:
   - Filesystem MCP server for file operations
   - Database MCP server for SQL databases
   - API MCP servers for external services

2. **Skill Chaining**: Multi-step workflows across skills

3. **Async Execution**: Non-blocking skill execution

4. **Caching**: Cache MCP results for repeated queries

5. **Observability**: 
   - Metrics (Prometheus)
   - Distributed tracing (OpenTelemetry)
   - Structured logging

---

## Glossary

- **MCP**: Model Context Protocol - A standardized protocol for tool/resource integration
- **Skill**: A capability that the agent can execute
- **JSON-RPC**: Remote procedure call protocol using JSON
- **stdio**: Standard input/output communication channel
- **osquery**: SQL-based operating system instrumentation framework

---

**Document Version:** 2.0  
**Last Modified:** November 8, 2025  
**Author:** Spring AI Agent Team
