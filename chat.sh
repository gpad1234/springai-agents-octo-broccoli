#!/bin/bash
# Interactive Chat Mode Launcher for Spring AI Agent

echo "Starting Spring AI Agent in Interactive Chat Mode..."
echo ""

# Check if JAR exists
JAR_FILE="target/springai-agent-demo-0.0.1-SNAPSHOT.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "JAR file not found. Building the project..."
    mvn clean package -DskipTests
    echo ""
fi

# Run in interactive chat mode
java -jar "$JAR_FILE" --chat
