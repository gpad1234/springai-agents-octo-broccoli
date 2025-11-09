# Spring AI Agent Demo - Quick Reference Card

## 🚀 Start Application
```bash
mvn spring-boot:run
```
**URL:** `http://localhost:8080`

---

## 📡 API Endpoint

**POST** `/api/agent/execute`

**Request Body:**
```json
{"goal": "your command here"}
```

---

## 🎯 Skills & Commands

### 🧮 Calculator
```bash
curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal": "calculate: 10 + 5"}'
```

**Triggers:** calculate, compute, sum, math operators  
**Example Goals:**
- `calculate: 20 * 3`
- `compute: 100 / 4`
- `sum: 15 - 7`

### 🔍 Search
```bash
curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal": "search: Spring Boot"}'
```

**Triggers:** search, find, lookup  
**Example Goals:**
- `search: Java tutorials`
- `find: best practices`
- `lookup: documentation`

### 📝 Summarize
```bash
curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{"goal": "summarize: Long text here..."}'
```

**Triggers:** summarize, summary, summarise  
**Example Goals:**
- `summarize: Article text...`
- `summary: Document content...`

---

## 🛠️ Common Commands

| Task | Command |
|------|---------|
| **Build** | `mvn clean install` |
| **Test** | `mvn test` |
| **Run** | `mvn spring-boot:run` |
| **Package** | `mvn package` |
| **Run JAR** | `java -jar target/springai-agent-demo-0.0.1-SNAPSHOT.jar` |

---

## 📁 Key Files

| File | Purpose |
|------|---------|
| `AgentService.java` | Core orchestration logic |
| `Skill.java` | Skill interface |
| `AgentController.java` | REST API endpoint |
| `ActionResult.java` | Response model |
| `application.properties` | Configuration |

**Location:** `src/main/java/com/example/agentdemo/`

---

## 🧩 Add New Skill

```java
@Component
public class YourSkill implements Skill {
    @Override
    public boolean canHandle(String goal) {
        return goal.toLowerCase().contains("keyword");
    }
    
    @Override
    public ActionResult execute(String goal) {
        // Your logic here
        return new ActionResult(true, "YourSkill", result);
    }
}
```

---

## 📊 Response Format

```json
{
  "goal": "original goal string",
  "trace": [
    {
      "success": true,
      "skillName": "SkillName",
      "output": "result data"
    }
  ],
  "finalOutput": "result data"
}
```

---

## 🔧 Configuration

**File:** `src/main/resources/application.properties`

```properties
server.port=8080
logging.level.com.example.agentdemo=INFO
```

---

## 📚 Documentation

| Document | Purpose |
|----------|---------|
| [README.md](../README.md) | Project overview |
| [ARCHITECTURE.md](docs/ARCHITECTURE.md) | System design |
| [TECHNICAL_SPEC.md](docs/TECHNICAL_SPEC.md) | API & specs |
| [INTERACTION_DIAGRAMS.md](docs/INTERACTION_DIAGRAMS.md) | Workflows |
| [VISUAL_SUMMARY.md](docs/VISUAL_SUMMARY.md) | Quick visuals |

---

## 🐛 Troubleshooting

| Issue | Solution |
|-------|----------|
| **Port 8080 in use** | Change in `application.properties` |
| **Build fails** | Run `mvn clean install` |
| **Tests fail** | Check logs in `target/surefire-reports/` |
| **No skill found** | Verify goal format matches skill triggers |

---

## 🎯 Architecture Layers

```
┌─────────────────────┐
│  AgentController    │ ← REST API
├─────────────────────┤
│  AgentService       │ ← Orchestration
├─────────────────────┤
│  Skills             │ ← Execution
│  • Calculator       │
│  • Search           │
│  • Summarize        │
└─────────────────────┘
```

---

## 💡 Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test
```bash
mvn test -Dtest=AgentServiceTest
```

### Test with Coverage
```bash
mvn test jacoco:report
```

---

## 🐳 Docker

### Build Image
```bash
docker build -t spring-ai-agent .
```

### Run Container
```bash
docker run -p 8080:8080 spring-ai-agent
```

---

## 📈 Tech Stack

- **Java:** 21 (LTS)
- **Spring Boot:** 3.2.11
- **Maven:** 3.x
- **Testing:** JUnit 5
- **Server:** Embedded Tomcat

---

## 🔗 Useful Links

- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [Maven Guide](https://maven.apache.org/guides/)
- [Java 21 Features](https://openjdk.org/projects/jdk/21/)

---

## 📝 Example Session

```bash
# Start application
$ mvn spring-boot:run

# In another terminal, test calculator
$ curl -X POST http://localhost:8080/api/agent/execute \
    -H "Content-Type: application/json" \
    -d '{"goal": "calculate: 42 / 6"}'

# Response:
# {
#   "goal": "calculate: 42 / 6",
#   "trace": [
#     {"success": true, "skillName": "CalculatorSkill", "output": "7.0"}
#   ],
#   "finalOutput": "7.0"
# }
```

---

**Version:** 0.0.1-SNAPSHOT  
**Updated:** November 8, 2025  
**Port:** 8080
