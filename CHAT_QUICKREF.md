# Interactive Chat Mode - Quick Reference

## Starting Interactive Chat

```bash
# Option 1: Using convenience script
./chat.sh

# Option 2: Using Maven
mvn spring-boot:run -Dspring-boot.run.arguments="--chat"

# Option 3: Using JAR directly
java -jar target/springai-agent-demo-0.0.1-SNAPSHOT.jar --chat

# Option 4: Short flag
java -jar target/springai-agent-demo-0.0.1-SNAPSHOT.jar -c
```

## Available Commands

### Calculator Skill
```
calculate: 10 + 5        → 15.0
compute: 20 * 3          → 60.0
calculate: 100 / 4       → 25.0
compute: 7.5 - 2.5       → 5.0
```

### Search Skill
```
search: Spring Boot      → Mock search results
find: Java features      → Mock search results
lookup: MCP protocol     → Mock search results
```

### Summarize Skill
```
summarize: <long text>   → Summary (first sentence or 120 chars)
tldr: <long text>        → Summary
summary: <long text>     → Summary
```

### Osquery MCP Skill (requires osquery server)
```
show system info         → System information from osquery
list users               → User list from osquery
osquery: SELECT * FROM system_info
```

### Exit Commands
```
exit                     → Terminate application
quit                     → Terminate application
EXIT                     → Terminate application (case insensitive)
QUIT                     → Terminate application (case insensitive)
```

## Supported Features

✅ Multiple skills (Calculator, Search, Summarize, Osquery)  
✅ Case-insensitive command recognition  
✅ Multiple keyword aliases per skill  
✅ Special characters and Unicode support  
✅ Error handling for invalid commands  
✅ Empty input gracefully handled  
✅ Immediate feedback on each request  

## Example Session

```
========================================
  Spring AI Agent - Interactive Chat
========================================
Available skills: Calculator, Search, Summarize, Osquery
Type 'exit' or 'quit' to stop
========================================

You: calculate: 42 / 7
Agent:
  [CalculatorSkill] 6.0

You: search: artificial intelligence
Agent:
  [MockSearchSkill] Mock search results for 'artificial intelligence':
1) Example result A - short description
2) Example result B - short description
3) Example result C - short description

You: tldr: The Model Context Protocol enables AI systems to connect with external tools
Agent:
  [SummarizeSkill] The Model Context Protocol enables AI systems to connect with external tools

You: exit
Goodbye!
```

## Testing

```bash
# Run all tests
mvn test

# Run only interactive chat tests
mvn test -Dtest=InteractiveChatTest

# Build and run
mvn clean package
./chat.sh
```

## Documentation

- **Test Scenarios:** CHAT_TEST_SCENARIOS.md (detailed test cases)
- **Test Results:** CHAT_TEST_RESULTS.md (validation report with 29 tests)
- **MCP Examples:** MCP_CHAT_EXAMPLES.md (real osquery query examples)
- **Main README:** README.md (complete documentation)
- **Demo Script:** ./demo-chat.sh (command examples)

## Limitations

⚠ No multi-line input support  
⚠ No command history (consider using `rlwrap java -jar ...`)  
⚠ No auto-completion  
⚠ Ctrl+C terminates immediately  

## Tips

💡 Use `rlwrap` for command history:
```bash
rlwrap java -jar target/springai-agent-demo-0.0.1-SNAPSHOT.jar --chat
```

💡 Pipe commands for automated testing:
```bash
echo -e "calculate: 5 + 5\nexit" | java -jar target/springai-agent-demo-0.0.1-SNAPSHOT.jar --chat
```

💡 Run in web server mode (default):
```bash
mvn spring-boot:run
# Access REST API at http://localhost:8080/api/agent/execute
```
