#!/bin/bash
# Quick Interactive Chat Demo
# This script demonstrates the interactive chat mode with example commands

echo "========================================"
echo "  Interactive Chat Mode - Quick Demo"
echo "========================================"
echo ""
echo "This demo will show you example commands you can use in interactive chat mode."
echo ""
echo "Starting the application in chat mode in 3 seconds..."
sleep 3

# Create a temporary script with example commands
cat > /tmp/chat_demo_commands.txt << 'EOF'
calculate: 15 + 27
search: Spring AI documentation
summarize: The Spring AI Agent is a modular system that combines local skills with MCP integration for extensible intelligence.
compute: 100 / 4
tldr: Model Context Protocol (MCP) is a standardized way to connect AI systems with external tools and data sources.
find: Java features
calculate: 3.14 * 2
exit
EOF

echo ""
echo "Demo commands prepared:"
echo "  1. calculate: 15 + 27"
echo "  2. search: Spring AI documentation"
echo "  3. summarize: <text>"
echo "  4. compute: 100 / 4"
echo "  5. tldr: <text>"
echo "  6. find: Java features"
echo "  7. calculate: 3.14 * 2"
echo "  8. exit"
echo ""
echo "Press Enter to start the demo (commands will be executed automatically)..."
read

# Note: This is a demonstration script
# For actual automated demo, you would need to pipe commands or use expect
echo ""
echo "To run the interactive chat manually, use:"
echo "  ./chat.sh"
echo "  OR"
echo "  java -jar target/springai-agent-demo-0.0.1-SNAPSHOT.jar --chat"
echo ""
echo "Available Commands:"
echo "  Calculator:  calculate: <expression> | compute: <expression>"
echo "  Search:      search: <query> | find: <query> | lookup: <query>"
echo "  Summarize:   summarize: <text> | tldr: <text> | summary: <text>"
echo "  Osquery:     osquery: <sql> | show system info | list users"
echo "  Exit:        exit | quit"
echo ""
