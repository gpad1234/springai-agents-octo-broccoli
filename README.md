# Spring AI Agent Demo with MCP Integration

A modular, skill-based intelligent agent system built with Spring Boot 3.2.11 and Java 21. This application demonstrates a **hybrid architecture** combining local skills with **Model Context Protocol (MCP)** integration, enabling extensibility through external tool servers while maintaining high performance for built-in capabilities.

## 📋 Features

- **RESTful API**: POST endpoint at `/api/agent/execute` with JSON payloads
- **Skill Discovery**: GET endpoint at `/api/agent/skills` lists all registered skills
- **Automatic Registration**: New skills are auto-discovered at startup via `@Component` annotation
- **Startup Logging**: Skills are displayed in the console when the application starts
- **Hybrid Skill System**: Combines local and MCP-based skills
- **Built-in Local Skills**:
  - 🧮 **CalculatorSkill**: Arithmetic operations (+, -, *, /)
  - 🔍 **MockSearchSkill**: Simulated information retrieval
  - 📝 **SummarizeSkill**: Text summarization
- **MCP Integration**:
  - 🖥️ **OsqueryMCPSkill**: System information queries via osquery MCP server
  - 🔌 JSON-RPC 2.0 protocol over stdio transport
  - 🔧 Extensible to any MCP-compliant server
- **Execution Tracing**: Complete visibility into skill selection and execution
- **Strategy Pattern**: Dynamic skill selection based on goal analysis

## 🏗️ Architecture

This application follows a **hybrid architecture** combining local skills with MCP-based external integrations:

```
┌─────────────────────────────────────────────┐
│        Presentation Layer                   │
│      (AgentController - REST API)           │
└──────────────┬──────────────────────────────┘
               │
┌──────────────▼──────────────────────────────┐
│        Business Logic Layer                 │
│      (AgentService - Orchestration)         │
└──────────────┬──────────────────────────────┘
               │
       ┌───────┴────────┐
       │                │
┌──────▼──────┐   ┌────▼─────────────────────┐
│Local Skills │   │   MCP Skills              │
│             │   │ (via MCPClientService)    │
│ Calculator  │   │                           │
│ Summarize   │   │  ┌─────────────────────┐ │
│ MockSearch  │   │  │  MCP Server         │ │
│             │   │  │  (osquery)          │ │
└─────────────┘   │  │  JSON-RPC 2.0       │ │
                  │  │  stdio transport    │ │
                  │  └─────────────────────┘ │
                  └──────────────────────────┘
```

### Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Java | 21 (LTS) |
| Framework | Spring Boot | 3.2.11 |
| Build Tool | Maven | 3.x |
| JSON Processing | Jackson | 2.15.4 |
| Async HTTP | Spring WebFlux | 6.1.14 |
| Process Management | Apache Commons Exec | 1.4.0 |
| MCP Protocol | JSON-RPC 2.0 | stdio |
| Testing | JUnit 5 | (bundled) |
| Server | Embedded Tomcat | (bundled) |

## �️ Adding New Skills

This application uses a simple, extensible pattern for adding new skills. Skills are automatically registered via Spring's component scanning.

### Step-by-Step Guide

#### 1. Create a New Skill Class

Create a new Java class in `src/main/java/com/example/agentdemo/agent/skills/`:

```java
package com.example.agentdemo.agent.skills;

import com.example.agentdemo.agent.Skill;
import com.example.agentdemo.model.ActionResult;
import org.springframework.stereotype.Component;

@Component  // This annotation auto-registers the skill
public class YourSkillName implements Skill {
    
    @Override
    public boolean canHandle(String goal) {
        // Define when this skill should be activated
        String g = goal.toLowerCase();
        return g.contains("your-trigger-word") || 
               g.contains("another-trigger");
    }
    
    @Override
    public ActionResult execute(String goal) {
        // Implement your skill logic
        try {
            String result = performTask(goal);
            return new ActionResult(true, "YourSkillName", result);
        } catch (Exception e) {
            return new ActionResult(false, "YourSkillName", 
                "Error: " + e.getMessage());
        }
    }
    
    private String performTask(String goal) {
        // Your implementation here
        return "Task completed!";
    }
}
```

#### 2. That's It! 🎉

The skill is automatically registered and available. No configuration needed.

### Real Example: WeatherSkill

Here's a complete working example:

```java
@Component
public class WeatherSkill implements Skill {
    
    @Override
    public boolean canHandle(String goal) {
        String g = goal.toLowerCase();
        return g.contains("weather") || 
               g.contains("temperature") || 
               g.contains("forecast");
    }
    
    @Override
    public ActionResult execute(String goal) {
        String city = extractCity(goal);
        String weather = generateWeather(city);
        return new ActionResult(true, "WeatherSkill", weather);
    }
}
```

**Usage Examples:**
- `weather in Seattle`
- `what's the temperature in Tokyo`
- `forecast for Paris`

### Skill Pattern Best Practices

✅ **DO:**
- Use descriptive class names ending in "Skill"
- Annotate with `@Component` for auto-registration
- Include clear trigger words in `canHandle()`
- Return success/failure in `ActionResult`
- Handle exceptions gracefully
- Extract input parsing into helper methods
- Add JavaDoc comments

❌ **DON'T:**
- Hardcode configuration values (use `@Value` for properties)
- Throw uncaught exceptions
- Overlap trigger words with other skills
- Make blocking calls without timeout
- Return null from `execute()`

### Available Skills (Reference)

| Skill | Triggers | Examples |
|-------|----------|----------|
| **CalculatorSkill** | calculate, compute, sum | `calculate 15 * 23` |
| **MockSearchSkill** | search, find, lookup | `search for AI news` |
| **SummarizeSkill** | summarize, summary, tldr | `summarize: [text]` |
| **OsqueryMCPSkill** | osquery, system query | `osquery: list processes` |
| **WeatherSkill** | weather, temperature, forecast | `weather in Seattle` |

## �📚 Documentation

Comprehensive documentation is available:
- **[Skills Usage Guide](SKILLS_USAGE_GUIDE.md)** - Complete guide to querying and using all skills
- **[Skills Quick Reference](SKILLS_QUICKREF.txt)** - One-page cheat sheet for all skills- **[Architecture Documentation V2](ARCHITECTURE_V2.md)** - Complete system design with MCP integration
- **[MCP Integration Guide](MCP_INTEGRATION_GUIDE.md)** - Setup and usage guide for Model Context Protocol
- **[Interactive Chat Quick Reference](CHAT_QUICKREF.md)** - Quick start guide for chat mode
- **[Chat Test Scenarios](CHAT_TEST_SCENARIOS.md)** - Comprehensive test scenarios documentation
- **[Chat Test Results](CHAT_TEST_RESULTS.md)** - Test validation report (29 tests passing)

## 🚀 Quick Start

### Prerequisites

- Java 21 or higher
- Maven 3.x
- Python 3.x (for osquery MCP server)
- osquery 5.x (optional, for MCP integration)

### Build and Test

```bash
mvn clean install
```

Run tests only:
```bash
mvn test
```

### Run Application

**Web Server Mode (default):**
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

**Interactive Chat Mode:**
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--chat"
```

Or using the JAR:
```bash
java -jar target/springai-agent-demo-0.0.1-SNAPSHOT.jar --chat
```

You can also use the short flag:
```bash
java -jar target/springai-agent-demo-0.0.1-SNAPSHOT.jar -c
```

Or use the convenience script:
```bash
./chat.sh
```

In interactive mode, you can directly type your requests:
```
========================================
  Spring AI Agent - Interactive Chat
========================================
Available skills: Calculator, Search, Summarize, Osquery
Type 'exit' or 'quit' to stop
========================================

You: calculate: 10 + 5
Agent:
  [CalculatorSkill] 15.0

You: search: spring boot
Agent:
  [MockSearchSkill] Mock search results for 'spring boot': ...

You: exit
Goodbye!
```

### MCP Server Setup (Optional)

To enable osquery MCP integration:

1. Install osquery: https://osquery.io/downloads
2. The Python MCP server (`osquery-mcp-server.py`) should be in your home directory
3. Configure in `application.properties`:
```properties
mcp.osquery.enabled=true
mcp.osquery.command=/home/your-user/osquery-mcp-server.py
mcp.osquery.args=
```

See [MCP_INTEGRATION_GUIDE.md](MCP_INTEGRATION_GUIDE.md) for detailed setup instructions.

## 📡 API Usage

### List Available Skills

**Endpoint:** `GET /api/agent/skills`

**Request:**
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

This endpoint automatically displays all registered skills. When you add a new skill, it will appear here immediately after restart.

### Execute Agent Goal

**Endpoint:** `POST /api/agent/execute`

**Request:**
```bash
curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal": "calculate: 10 + 5"}'
```

**Response:**
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

### Example Goals

#### Calculator (Local Skill)
```json
{"goal": "calculate: 20 * 3"}
{"goal": "compute: 100 / 4"}
```

#### Search (Local Skill)
```json
{"goal": "search: Spring Boot documentation"}
{"goal": "find: Java 21 features"}
```

#### Summarize (Local Skill)
```json
{"goal": "summarize: The quick brown fox jumps over the lazy dog. It is a pangram containing all alphabet letters."}
```

#### System Information (MCP Skill - requires osquery)
```json
{"goal": "show system info"}
{"goal": "list users"}
{"goal": "osquery: SELECT * FROM system_info"}
```

## 🔧 Adding New Skills

### Local Skills

1. Create a class implementing the `Skill` interface
2. Add `@Component` annotation
3. Implement `canHandle(String goal)` - determines if skill can process the goal
4. Implement `execute(String goal)` - performs the actual work

**Example:**
```java
@Component
public class TranslateSkill implements Skill {
    @Override
    public boolean canHandle(String goal) {
        return goal != null && goal.toLowerCase().contains("translate");
    }
    
    @Override
    public ActionResult execute(String goal) {
        // Your translation logic here
        return new ActionResult(true, "TranslateSkill", translatedText);
    }
}
```

### MCP-Based Skills

1. Create a class implementing `Skill` interface
2. Inject `MCPClientService`
3. Connect to an MCP server in `@PostConstruct`
4. Use `mcpClient.callTool()` in the execute method

**Example:**
```java
@Component
public class WeatherMCPSkill implements Skill {
    @Autowired
    private MCPClientService mcpClient;
    
    @PostConstruct
    public void init() {
        mcpClient.connectServer("weather", "weather-mcp-server", List.of());
    }
    
    @Override
    public ActionResult execute(String goal) {
        MCPToolCall toolCall = MCPToolCall.builder()
            .name("get_weather")
            .arguments(Map.of("city", extractCity(goal)))
            .build();
        MCPToolResult result = mcpClient.callTool("weather", toolCall);
        return new ActionResult(true, "WeatherMCPSkill", extractText(result));
    }
}
```

Skills are automatically discovered by Spring and added to the agent's capabilities.

## 🧪 Testing

All tests passing (6/6):

### Local Skills
- ✅ CalculatorSkill: Arithmetic operations
- ✅ SummarizeSkill: Text summarization  
- ✅ MockSearchSkill: Mock search results

### MCP Integration
- ✅ OsqueryMCPSkill: Hostname queries
- ✅ OsqueryMCPSkill: System information
- ✅ OsqueryMCPSkill: User listing

Run the complete test suite:
```bash
mvn test
```

See [Test Results](/tmp/test_results.md) for detailed validation.

Current test coverage includes:
- Unit tests for `AgentService`
- Skill execution tests
- Integration tests with Spring context
- MCP protocol integration tests

## 📦 Building for Production

Create an executable JAR:
```bash
mvn clean package -DskipTests
```

The JAR will be created at: `target/springai-agent-demo-0.0.1-SNAPSHOT.jar`

Run the JAR:
```bash
java -jar target/springai-agent-demo-0.0.1-SNAPSHOT.jar
```

## 🐳 Docker Support

Create a `Dockerfile`:
```dockerfile
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY target/springai-agent-demo-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and run:
```bash
docker build -t spring-ai-agent-demo .
docker run -p 8080:8080 spring-ai-agent-demo
```

## 🔍 Project Structure

```
springai_agent_getting_started/
├── src/
│   ├── main/
│   │   ├── java/com/example/agentdemo/
│   │   │   ├── AgentDemoApplication.java
│   │   │   ├── agent/
│   │   │   │   ├── AgentService.java       # Core orchestration
│   │   │   │   ├── Skill.java              # Skill interface
│   │   │   │   └── skills/
│   │   │   │       ├── CalculatorSkill.java
│   │   │   │       ├── MockSearchSkill.java
│   │   │   │       ├── SummarizeSkill.java
│   │   │   │       └── OsqueryMCPSkill.java # MCP integration
│   │   │   ├── mcp/
│   │   │   │   ├── client/
│   │   │   │   │   └── MCPClientService.java   # MCP client
│   │   │   │   ├── config/
│   │   │   │   │   └── MCPConfiguration.java
│   │   │   │   └── model/
│   │   │   │       ├── JsonRpcRequest.java
│   │   │   │       ├── JsonRpcResponse.java
│   │   │   │       ├── MCPTool.java
│   │   │   │       ├── MCPToolCall.java
│   │   │   │       └── MCPToolResult.java
│   │   │   ├── controller/
│   │   │   │   └── AgentController.java    # REST API
│   │   │   └── model/
│   │   │       └── ActionResult.java       # Result model
│   │   └── resources/
│   │       └── application.properties      # MCP configuration
│   └── test/
│       └── java/com/example/agentdemo/
│           └── AgentServiceTest.java
├── ARCHITECTURE_V2.md                      # Architecture documentation
├── MCP_INTEGRATION_GUIDE.md                # MCP setup guide
├── pom.xml
└── README.md
```

## 🛠️ Configuration

Edit `src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=8080

# MCP Osquery Server Configuration
mcp.osquery.enabled=true
mcp.osquery.command=/home/your-user/osquery-mcp-server.py
mcp.osquery.args=

# Logging
logging.level.com.example.agentdemo=INFO
```

### MCP Configuration Options

| Property | Description | Default |
|----------|-------------|---------|
| `mcp.osquery.enabled` | Enable/disable osquery MCP integration | `false` |
| `mcp.osquery.command` | Path to MCP server executable | `npx` |
| `mcp.osquery.args` | Command-line arguments for MCP server | (empty) |

## 🎯 Design Patterns Used

- **Strategy Pattern**: Skills as interchangeable strategies
- **Dependency Injection**: Spring IoC for component management
- **Chain of Responsibility**: Skill selection mechanism
- **Template Method**: Uniform skill execution pattern
- **Proxy Pattern**: MCPClientService as proxy to external MCP servers
- **Builder Pattern**: Model objects (MCPToolCall, MCPToolResult)

## � Security

### Sensitive Data Protection

This project includes comprehensive security measures to protect API keys, credentials, and sensitive configuration:

- **`.gitignore`**: Prevents accidental commit of secrets (API keys, .env files, credentials)
- **`.env.example`**: Template for environment variables (actual `.env` is gitignored)
- **`SECURITY.md`**: Complete security guidelines and best practices

### Quick Security Setup

1. **Copy environment template**:
   ```bash
   cp .env.example .env
   ```

2. **Add your secrets to `.env`** (never commit this file):
   ```bash
   # Edit with your actual values
   nano .env
   ```

3. **Verify .gitignore is working**:
   ```bash
   git status  # Should NOT show .env file
   ```

### What's Protected by .gitignore

✅ API keys and tokens (`.key`, `.pem` files)  
✅ Environment files (`.env`, `.env.*`)  
✅ Credentials and secrets  
✅ Local configuration files  
✅ Database files  
✅ SSH keys  
✅ Cloud provider credentials  

### Security Best Practices

❌ **Never commit**:
- API keys in code
- Passwords in `application.properties`
- `.env` files
- Private keys or certificates

✅ **Always use**:
- Environment variables for secrets
- `.env` file for local development (gitignored)
- Secrets management services in production
- Different credentials for dev/staging/prod

📖 **See [SECURITY.md](SECURITY.md) for complete guidelines**

## �🔮 Future Enhancements

- [ ] Add authentication and authorization (JWT, OAuth2)
- [ ] Implement async skill execution
- [ ] Add skill composition (multi-step workflows)
- [ ] Integrate additional MCP servers (filesystem, database, etc.)
- [ ] Add real AI/LLM capabilities
- [ ] Add metrics and observability (Spring Actuator)
- [ ] Implement caching layer
- [ ] Add rate limiting
- [ ] Implement API key authentication
- [ ] Add HTTPS/TLS support

## 📄 License

Demo / Educational Sample

---

**Version:** 0.0.1-SNAPSHOT  
**Last Updated:** November 8, 2025  
**Java Version:** 21 (LTS)  
**Spring Boot Version:** 3.2.11  
**MCP Protocol:** JSON-RPC 2.0
