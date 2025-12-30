# Skills Usage Guide

Complete guide to discovering and using agent skills.

## 📋 Discovering Available Skills

### Method 1: Check Startup Logs
When the application starts, you'll see all registered skills:
```
============================================================
🤖 Agent Service Initialized with 5 skill(s)
============================================================
  1. CalculatorSkill - Ready
  2. MockSearchSkill - Ready
  3. OsqueryMCPSkill - Ready
  4. SummarizeSkill - Ready
  5. WeatherSkill - Ready
============================================================
```

### Method 2: Query the API
```bash
curl http://localhost:8080/api/agent/skills
```

**Response:**
```json
{
  "count": 5,
  "skills": [
    "CalculatorSkill",
    "MockSearchSkill",
    "OsqueryMCPSkill",
    "SummarizeSkill",
    "WeatherSkill"
  ]
}
```

## 🎯 Using Skills via API

All skills are executed through the `/api/agent/execute` endpoint.

### Basic Pattern
```bash
curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal": "your command here"}'
```

## 📚 Skill Reference & Examples

### 1. CalculatorSkill 🧮

**Trigger words:** `calculate`, `compute`, `sum`, `add`, `subtract`, `multiply`, `divide`

**Examples:**
```bash
# Addition
curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal": "calculate: 10 + 5"}'

# Multiplication
curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal": "compute: 25 * 4"}'

# Division
curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal": "calculate: 100 / 4"}'

# Complex expression
curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal": "calculate: (10 + 5) * 3"}'
```

**Response format:**
```json
{
  "goal": "calculate: 10 + 5",
  "trace": [
    {
      "success": true,
      "skillName": "CalculatorSkill",
      "output": "15.0"
    }
  ],
  "finalOutput": "15.0"
}
```

---

### 2. MockSearchSkill 🔍

**Trigger words:** `search`, `find`, `lookup`, `query`

**Examples:**
```bash
# Basic search
curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal": "search: Spring Boot documentation"}'

# Find information
curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal": "find: Java 21 features"}'

# Lookup
curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal": "lookup: MCP protocol"}'
```

**Response format:**
```json
{
  "goal": "search: Spring Boot documentation",
  "trace": [
    {
      "success": true,
      "skillName": "MockSearchSkill",
      "output": "Mock search results for 'Spring Boot documentation':\n1) Example result A - short description\n2) Example result B - short description\n3) Example result C - short description"
    }
  ],
  "finalOutput": "Mock search results for 'Spring Boot documentation':..."
}
```

---

### 3. SummarizeSkill 📝

**Trigger words:** `summarize`, `summary`, `tldr`, `brief`

**Examples:**
```bash
# Summarize text
curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal": "summarize: The quick brown fox jumps over the lazy dog. This is a famous pangram that contains every letter of the English alphabet at least once. It is commonly used for testing fonts and keyboards."}'

# Get summary
curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal": "tldr: Long text here..."}'
```

**Response format:**
```json
{
  "goal": "summarize: The quick brown fox...",
  "trace": [
    {
      "success": true,
      "skillName": "SummarizeSkill",
      "output": "The quick brown fox jumps over the lazy dog."
    }
  ],
  "finalOutput": "The quick brown fox jumps over the lazy dog."
}
```

---

### 4. OsqueryMCPSkill 🖥️

**Trigger words:** `osquery`, `system`, `hostname`, `users`

**Examples:**
```bash
# Get hostname
curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal": "what is the hostname"}'

# System info
curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal": "show system info"}'

# List users
curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal": "list users"}'

# Direct osquery
curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal": "osquery: SELECT * FROM system_info"}'
```

**Response format:**
```json
{
  "goal": "what is the hostname",
  "trace": [
    {
      "success": true,
      "skillName": "OsqueryMCPSkill",
      "output": "Hostname: your-hostname"
    }
  ],
  "finalOutput": "Hostname: your-hostname"
}
```

---

### 5. WeatherSkill 🌤️

**Trigger words:** `weather`, `temperature`, `forecast`

**Examples:**
```bash
# Check weather
curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal": "weather in Seattle"}'

# Get temperature
curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal": "what is the temperature in Tokyo"}'

# Get forecast
curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal": "forecast for Paris"}'
```

**Response format:**
```json
{
  "goal": "weather in Seattle",
  "trace": [
    {
      "success": true,
      "skillName": "WeatherSkill",
      "output": "Weather in Seattle: Partly cloudy, 18°C (64°F), Humidity: 65%"
    }
  ],
  "finalOutput": "Weather in Seattle: Partly cloudy, 18°C (64°F), Humidity: 65%"
}
```

---

## 🔄 Interactive Chat Mode

For a more conversational interface, use the interactive chat mode:

```bash
./chat.sh
```

Then type your commands naturally:
```
You: calculate: 10 + 5
Agent: [CalculatorSkill] 15.0

You: weather in Seattle
Agent: [WeatherSkill] Weather in Seattle: Partly cloudy, 18°C (64°F)

You: search: Spring AI
Agent: [MockSearchSkill] Mock search results for 'Spring AI': ...
```

## 🎨 Response Format

All skill executions return the same structure:

```json
{
  "goal": "original user input",
  "trace": [
    {
      "success": true/false,
      "skillName": "SkillNameThatHandled",
      "output": "result from the skill"
    }
  ],
  "finalOutput": "final result"
}
```

## 🔍 Skill Selection Logic

Skills are evaluated in order until one declares it can handle the goal:

1. Agent receives goal
2. Each skill's `canHandle(goal)` method is checked
3. First matching skill executes
4. Result is returned

**Example:**
```
Goal: "calculate: 10 + 5"
  ✓ CalculatorSkill.canHandle() → true (contains "calculate")
  → CalculatorSkill.execute() → "15.0"
```

## 🚀 Testing All Skills

Use the test script to quickly verify all skills:

```bash
./demo-chat.sh
```

Or test manually:
```bash
# Test each skill
curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal": "calculate: 5 + 3"}'

curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal": "search: test query"}'

curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal": "summarize: This is a test"}'

curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal": "weather in London"}'

curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal": "what is the hostname"}'
```

## 💡 Tips

1. **Natural Language**: Skills are triggered by keywords, so you can use natural language
2. **Case Insensitive**: Trigger words work regardless of case
3. **Skill Priority**: Skills are checked in registration order
4. **Error Handling**: Failed skills return `"success": false` with error details
5. **Extensibility**: Add new skills by implementing the `Skill` interface with `@Component`

## 📊 Monitoring

Check application logs to see which skill handled each request:
```bash
tail -f /tmp/app.log
```

Or use the interactive mode for real-time feedback.
