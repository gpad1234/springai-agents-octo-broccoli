# MCP/Osquery Interactive Chat Examples

This document provides real-world examples of using the Osquery MCP server integration in interactive chat mode.

## Prerequisites

Ensure the osquery MCP server is enabled in `application.properties`:
```properties
mcp.osquery.enabled=true
mcp.osquery.command=./osquery-mcp-server.py
```

## Tested Sample Queries

### 1. Get Hostname

**Natural Language:**
```
You: what is the hostname
Agent:
  [OsqueryMCPSkill] [
  {
    "hostname": "gpwin10."
  }
]
```

**Simple Keyword:**
```
You: hostname
Agent:
  [OsqueryMCPSkill] [
  {
    "hostname": "gpwin10."
  }
]
```

### 2. System Information

```
You: show system info
Agent:
  [OsqueryMCPSkill] [
  {
    "board_model": "",
    "board_serial": "",
    "board_vendor": "",
    "board_version": "",
    "computer_name": "gpwin10",
    "cpu_brand": "Intel(R) Core(TM) i7-8650U CPU @ 1.90GHz",
    "cpu_logical_cores": "8",
    "cpu_microcode": "0xffffffff",
    "cpu_physical_cores": "4",
    "cpu_sockets": "1",
    "cpu_subtype": "142",
    "cpu_type": "x86_64",
    "hardware_model": "",
    "hardware_serial": "",
    "hardware_vendor": "",
    "hardware_version": "",
    "hostname": "gpwin10.",
    "local_hostname": "gpwin10",
    "physical_memory": "16936366080",
    "uuid": "..."
  }
]
```

### 3. List System Users

```
You: list users
Agent:
  [OsqueryMCPSkill] [
  {
    "shell": "/bin/bash",
    "uid": "0",
    "username": "root"
  },
  {
    "shell": "/usr/sbin/nologin",
    "uid": "1",
    "username": "daemon"
  },
  {
    "shell": "/usr/sbin/nologin",
    "uid": "2",
    "username": "bin"
  },
  {
    "shell": "/usr/sbin/nologin",
    "uid": "3",
    "username": "sys"
  },
  ...
]
```

### 4. What is the System

```
You: what is the system
Agent:
  [OsqueryMCPSkill] <system information>
```

### 5. Running Processes

```
You: running process
Agent:
  [OsqueryMCPSkill] <process list from osquery>
```

### 6. Network Connections

```
You: network connection
Agent:
  [OsqueryMCPSkill] <network connections from osquery>
```

## Supported Keywords

The OsqueryMCPSkill recognizes the following patterns:

- `osquery:` - Direct osquery prefix
- `system info` - System information queries
- `running process` - Process listing
- `network connection` - Network information
- `list users` - User information
- `system query` - General system queries
- `hostname` - Hostname queries
- `what is the system` - Natural language system queries
- `what is the computer` - Natural language computer queries

## Complete Test Session Example

```
========================================
  Spring AI Agent - Interactive Chat
========================================
Available skills: Calculator, Search, Summarize, Osquery
Type 'exit' or 'quit' to stop
========================================

You: hostname
Agent:
  [OsqueryMCPSkill] [
  {
    "hostname": "gpwin10."
  }
]

You: calculate: 10 + 5
Agent:
  [CalculatorSkill] 15.0

You: show system info
Agent:
  [OsqueryMCPSkill] [
  {
    "computer_name": "gpwin10",
    "cpu_brand": "Intel(R) Core(TM) i7-8650U CPU @ 1.90GHz",
    "cpu_logical_cores": "8",
    "hostname": "gpwin10.",
    ...
  }
]

You: list users
Agent:
  [OsqueryMCPSkill] [
  {
    "shell": "/bin/bash",
    "uid": "0",
    "username": "root"
  },
  ...
]

You: exit
Goodbye!
```

## Automated Tests

All these scenarios are covered in the automated test suite:

```bash
# Run MCP/Osquery tests
mvn test -Dtest=InteractiveChatTest

# Tests include:
# - Scenario 4.1: System Info Query
# - Scenario 4.2: Get Hostname (Natural Language)
# - Scenario 4.3: Get Hostname (Simple Keyword)
# - Scenario 4.4: List Users
# - Scenario 4.7: What is the System
```

## Notes

- MCP server connection is established automatically on startup
- Clean disconnection happens on exit
- All queries return JSON-formatted results
- Tests are optional - they pass gracefully if MCP server is not available
- The osquery MCP server runs as a subprocess managed by the application

## Troubleshooting

**If MCP queries fail:**

1. Check that `mcp.osquery.enabled=true` in `application.properties`
2. Verify `osquery-mcp-server.py` is in the application directory
3. Ensure osquery is installed on your system
4. Check application logs for connection errors

**Logs to watch:**
```
INFO c.e.a.agent.skills.OsqueryMCPSkill : Connecting to osquery MCP server...
INFO c.e.a.mcp.client.MCPClientService : Successfully connected to MCP server: osquery
INFO c.e.a.agent.skills.OsqueryMCPSkill : Available osquery tools: query_osquery
```
