#!/usr/bin/env python3
"""
osquery MCP Server
Simple Model Context Protocol server for osquery integration
"""

import json
import sys
import os
import subprocess
from typing import Any, Optional

def run_osquery_command(sql: str) -> list[dict[str, Any]]:
    """Execute an osquery SQL query and return results"""
    try:
        # Use osqueryi for interactive queries
        process = subprocess.run(
            ['osqueryi', '--json', sql],
            capture_output=True,
            text=True,
            timeout=30
        )
        
        if process.returncode != 0:
            return []
        
        try:
            return json.loads(process.stdout)
        except json.JSONDecodeError:
            return []
    except (subprocess.TimeoutExpired, FileNotFoundError) as e:
        print(f"Error executing osquery: {e}", file=sys.stderr)
        return []

def handle_initialize() -> dict:
    """Handle MCP initialize request"""
    return {
        "protocolVersion": "2024-11-05",
        "capabilities": {
            "tools": {}
        },
        "serverInfo": {
            "name": "osquery-mcp",
            "version": "1.0.0"
        }
    }

def handle_list_tools() -> dict:
    """List available tools"""
    return {
        "tools": [
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
        ]
    }

def handle_call_tool(tool_name: str, arguments: dict) -> dict:
    """Handle tool calls"""
    if tool_name == "query_osquery":
        sql = arguments.get("sql", "")
        if not sql:
            return {"error": "No SQL provided"}
        
        results = run_osquery_command(sql)
        return {
            "content": [
                {
                    "type": "text",
                    "text": json.dumps(results, indent=2)
                }
            ]
        }
    
    return {"error": f"Unknown tool: {tool_name}"}

def main():
    """Main server loop"""
    while True:
        try:
            # Read JSON-RPC request
            line = sys.stdin.readline().strip()
            if not line:
                continue
            
            request = json.loads(line)
            
            method = request.get("method")
            params = request.get("params", {})
            req_id = request.get("id")
            
            # Handle different MCP methods
            if method == "initialize":
                result = handle_initialize()
            elif method == "tools/list":
                result = handle_list_tools()
            elif method == "tools/call":
                tool_name = params.get("name")
                arguments = params.get("arguments", {})
                result = handle_call_tool(tool_name, arguments)
            else:
                # Send error response for unknown methods
                response = {
                    "jsonrpc": "2.0",
                    "id": req_id,
                    "error": {
                        "code": -32601,
                        "message": f"Method not found: {method}"
                    }
                }
                print(json.dumps(response))
                sys.stdout.flush()
                continue
            
            # Send success response wrapped in JSON-RPC envelope
            response = {
                "jsonrpc": "2.0",
                "id": req_id,
                "result": result
            }
            print(json.dumps(response))
            sys.stdout.flush()
        
        except json.JSONDecodeError:
            error_response = {
                "jsonrpc": "2.0",
                "id": None,
                "error": {
                    "code": -32700,
                    "message": "Parse error: Invalid JSON"
                }
            }
            print(json.dumps(error_response))
            sys.stdout.flush()
        except Exception as e:
            error_response = {
                "jsonrpc": "2.0",
                "id": None,
                "error": {
                    "code": -32603,
                    "message": f"Internal error: {str(e)}"
                }
            }
            print(json.dumps(error_response))
            sys.stdout.flush()

if __name__ == "__main__":
    main()
