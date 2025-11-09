# Spring AI Agent Demo - Technical Specification

## Table of Contents
1. [System Specification](#system-specification)
2. [API Specification](#api-specification)
3. [Interaction Diagrams](#interaction-diagrams)
4. [Sequence Diagrams](#sequence-diagrams)
5. [Data Flow](#data-flow)
6. [Component Interactions](#component-interactions)
7. [Error Handling](#error-handling)

---

## System Specification

### Application Metadata
- **Project Name**: springai-agent-demo
- **Group ID**: com.example
- **Artifact ID**: springai-agent-demo
- **Version**: 0.0.1-SNAPSHOT
- **Packaging**: JAR
- **Java Version**: 21
- **Spring Boot Version**: 3.2.11

### Runtime Configuration
- **Server Port**: 8080
- **Context Path**: /
- **Base API Path**: /api/agent

---

## API Specification

### Endpoint: Execute Agent Goal

#### Request
```http
POST /api/agent/execute
Content-Type: application/json

{
  "goal": "calculate: 10 + 5"
}
```

#### Response (Success)
```http
HTTP/1.1 200 OK
Content-Type: application/json

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

#### Response (No Skill Found)
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "goal": "unknown command",
  "trace": [
    {
      "success": false,
      "skillName": "none",
      "output": "No skill found to handle goal: unknown command"
    }
  ],
  "finalOutput": "No skill found to handle goal: unknown command"
}
```

### Supported Goal Formats

| Goal Type | Example | Handler Skill |
|-----------|---------|---------------|
| **Calculation** | `calculate: 2 + 3` | CalculatorSkill |
| | `compute: 10 * 5` | CalculatorSkill |
| | `sum: 7 - 2` | CalculatorSkill |
| **Search** | `search: Spring Boot docs` | MockSearchSkill |
| | `find: Java tutorials` | MockSearchSkill |
| | `lookup: best practices` | MockSearchSkill |
| **Summarization** | `summarize: Long text here...` | SummarizeSkill |
| | `summary: Article content...` | SummarizeSkill |

---

## Interaction Diagrams

### 1. Complete Request Flow

```mermaid
sequenceDiagram
    actor Client
    participant Controller as AgentController
    participant Service as AgentService
    participant Calc as CalculatorSkill
    participant Search as MockSearchSkill
    participant Sum as SummarizeSkill
    
    Client->>Controller: POST /api/agent/execute<br/>{goal: "calculate: 5+3"}
    activate Controller
    
    Controller->>Service: executeGoal("calculate: 5+3")
    activate Service
    
    Service->>Calc: canHandle("calculate: 5+3")?
    activate Calc
    Calc-->>Service: true
    deactivate Calc
    
    Service->>Calc: execute("calculate: 5+3")
    activate Calc
    Calc->>Calc: Parse expression
    Calc->>Calc: Perform calculation
    Calc-->>Service: ActionResult(success=true,<br/>skillName="CalculatorSkill",<br/>output="8.0")
    deactivate Calc
    
    Service-->>Controller: List<ActionResult>
    deactivate Service
    
    Controller->>Controller: Build response map
    Controller-->>Client: {goal, trace, finalOutput}
    deactivate Controller
```

### 2. Skill Selection Process

```mermaid
sequenceDiagram
    participant Service as AgentService
    participant Skill1 as CalculatorSkill
    participant Skill2 as MockSearchSkill
    participant Skill3 as SummarizeSkill
    
    Note over Service: Goal: "search: Java docs"
    
    Service->>Skill1: canHandle("search: Java docs")?
    Skill1-->>Service: false
    
    Service->>Skill2: canHandle("search: Java docs")?
    Skill2-->>Service: true
    
    Service->>Skill2: execute("search: Java docs")
    activate Skill2
    Skill2->>Skill2: Extract query
    Skill2->>Skill2: Generate mock results
    Skill2-->>Service: ActionResult
    deactivate Skill2
    
    Note over Service,Skill3: Skill3 not checked<br/>(match found)
```

### 3. No Skill Found Flow

```mermaid
sequenceDiagram
    participant Service as AgentService
    participant Skill1 as CalculatorSkill
    participant Skill2 as MockSearchSkill
    participant Skill3 as SummarizeSkill
    
    Note over Service: Goal: "unknown: xyz"
    
    Service->>Skill1: canHandle("unknown: xyz")?
    Skill1-->>Service: false
    
    Service->>Skill2: canHandle("unknown: xyz")?
    Skill2-->>Service: false
    
    Service->>Skill3: canHandle("unknown: xyz")?
    Skill3-->>Service: false
    
    Service->>Service: Create fallback ActionResult
    Note over Service: ActionResult(<br/>success=false,<br/>skillName="none",<br/>output="No skill found...")
```

---

## Sequence Diagrams

### 1. Calculation Request Flow

```mermaid
sequenceDiagram
    actor User
    participant API as REST API
    participant Controller as AgentController
    participant Service as AgentService
    participant Skill as CalculatorSkill
    
    User->>API: POST /api/agent/execute<br/>{"goal": "calculate: 15 / 3"}
    API->>Controller: Route request
    
    Controller->>Controller: Extract goal from body
    
    Controller->>Service: executeGoal("calculate: 15 / 3")
    
    Service->>Service: Iterate through skills
    
    Service->>Skill: canHandle("calculate: 15 / 3")?
    Note over Skill: Check for "calculate"<br/>keyword
    Skill-->>Service: true
    
    Service->>Skill: execute("calculate: 15 / 3")
    
    Skill->>Skill: Extract expression: "15 / 3"
    Skill->>Skill: Parse: a=15, op=/, b=3
    Skill->>Skill: Compute: 15 / 3 = 5.0
    
    Skill-->>Service: ActionResult(<br/>success=true,<br/>skillName="CalculatorSkill",<br/>output="5.0")
    
    Service-->>Controller: List[ActionResult]
    
    Controller->>Controller: Format response:<br/>- goal<br/>- trace<br/>- finalOutput
    
    Controller-->>API: ResponseEntity<Map>
    API-->>User: HTTP 200 OK<br/>JSON response
```

### 2. Search Request Flow

```mermaid
sequenceDiagram
    actor User
    participant API as REST API
    participant Controller as AgentController
    participant Service as AgentService
    participant Skill as MockSearchSkill
    
    User->>API: POST /api/agent/execute<br/>{"goal": "search: Spring Boot"}
    API->>Controller: Route request
    
    Controller->>Service: executeGoal("search: Spring Boot")
    
    Service->>Skill: canHandle("search: Spring Boot")?
    Note over Skill: Check for "search"<br/>keyword
    Skill-->>Service: true
    
    Service->>Skill: execute("search: Spring Boot")
    
    Skill->>Skill: Extract query: "Spring Boot"
    Skill->>Skill: Generate mock results
    
    Skill-->>Service: ActionResult(<br/>success=true,<br/>skillName="MockSearchSkill",<br/>output="Mock search results...")
    
    Service-->>Controller: List[ActionResult]
    
    Controller-->>API: ResponseEntity<Map>
    API-->>User: HTTP 200 OK<br/>JSON response
```

### 3. Summarization Request Flow

```mermaid
sequenceDiagram
    actor User
    participant API as REST API
    participant Controller as AgentController
    participant Service as AgentService
    participant Skill as SummarizeSkill
    
    User->>API: POST /api/agent/execute<br/>{"goal": "summarize: The quick brown fox jumps over the lazy dog. It is a classic pangram."}
    API->>Controller: Route request
    
    Controller->>Service: executeGoal("summarize: ...")
    
    Service->>Skill: canHandle("summarize: ...")?
    Note over Skill: Check for "summarize"<br/>keyword
    Skill-->>Service: true
    
    Service->>Skill: execute("summarize: ...")
    
    Skill->>Skill: Extract payload after ":"
    Skill->>Skill: Find first sentence
    Note over Skill: Extract up to first period
    Skill->>Skill: Create summary
    
    Skill-->>Service: ActionResult(<br/>success=true,<br/>skillName="SummarizeSkill",<br/>output="The quick brown fox jumps over the lazy dog.")
    
    Service-->>Controller: List[ActionResult]
    
    Controller-->>API: ResponseEntity<Map>
    API-->>User: HTTP 200 OK<br/>JSON response
```

---

## Data Flow

### Component Data Flow Diagram

```mermaid
flowchart LR
    A[HTTP Request<br/>JSON] --> B[AgentController]
    B --> C{Extract Goal}
    C --> D[AgentService]
    
    D --> E{Iterate Skills}
    
    E --> F[Skill.canHandle?]
    F -->|true| G[Skill.execute]
    F -->|false| H[Next Skill]
    H --> E
    
    G --> I[ActionResult]
    I --> J[Add to Trace]
    J --> K[Return Trace]
    
    E -->|No match| L[Fallback Result]
    L --> K
    
    K --> M[Build Response]
    M --> N[HTTP Response<br/>JSON]
    
    style A fill:#e3f2fd
    style N fill:#e3f2fd
    style D fill:#fff4e1
    style G fill:#e8f5e9
    style I fill:#fce4ec
```

### Data Models

```mermaid
classDiagram
    class ActionResult {
        -boolean success
        -String skillName
        -String output
        +ActionResult()
        +ActionResult(success, skillName, output)
        +isSuccess() boolean
        +getSkillName() String
        +getOutput() String
        +setSuccess(boolean)
        +setSkillName(String)
        +setOutput(String)
    }
    
    class RequestBody {
        +String goal
    }
    
    class ResponseBody {
        +String goal
        +List~ActionResult~ trace
        +String finalOutput
    }
    
    ResponseBody --> ActionResult : contains
```

---

## Component Interactions

### Class Diagram

```mermaid
classDiagram
    class AgentController {
        -AgentService agentService
        +AgentController(agentService)
        +execute(body) ResponseEntity
    }
    
    class AgentService {
        -List~Skill~ skills
        +AgentService(skills)
        +executeGoal(goal) List~ActionResult~
    }
    
    class Skill {
        <<interface>>
        +canHandle(goal) boolean
        +execute(goal) ActionResult
    }
    
    class CalculatorSkill {
        -Pattern SIMPLE_EXPR
        +canHandle(goal) boolean
        +execute(goal) ActionResult
    }
    
    class MockSearchSkill {
        +canHandle(goal) boolean
        +execute(goal) ActionResult
    }
    
    class SummarizeSkill {
        +canHandle(goal) boolean
        +execute(goal) ActionResult
    }
    
    class ActionResult {
        -boolean success
        -String skillName
        -String output
        +getters/setters
    }
    
    AgentController --> AgentService : uses
    AgentService --> Skill : manages
    Skill <|.. CalculatorSkill : implements
    Skill <|.. MockSearchSkill : implements
    Skill <|.. SummarizeSkill : implements
    Skill --> ActionResult : returns
    AgentController --> ActionResult : processes
```

### Object Interaction at Runtime

```mermaid
graph TB
    subgraph "Spring Context"
        AC[AgentController<br/>Bean]
        AS[AgentService<br/>Bean]
        CS[CalculatorSkill<br/>Bean]
        MS[MockSearchSkill<br/>Bean]
        SS[SummarizeSkill<br/>Bean]
    end
    
    AC -->|@Autowired| AS
    AS -->|@Autowired<br/>List injection| CS
    AS -->|@Autowired<br/>List injection| MS
    AS -->|@Autowired<br/>List injection| SS
    
    Client[HTTP Client] -->|Request| AC
    AC -->|Response| Client
    
    style AC fill:#e1f5ff
    style AS fill:#fff4e1
    style CS fill:#e8f5e9
    style MS fill:#e8f5e9
    style SS fill:#e8f5e9
    style Client fill:#ffebee
```

---

## Error Handling

### Error Scenarios

```mermaid
flowchart TD
    Start([Client Request]) --> ValidateJSON{Valid JSON?}
    
    ValidateJSON -->|No| E1[HTTP 400<br/>Bad Request]
    ValidateJSON -->|Yes| CheckGoal{Goal Present?}
    
    CheckGoal -->|No| DefaultGoal[Use Empty String]
    CheckGoal -->|Yes| ProcessGoal[Process Goal]
    
    DefaultGoal --> ProcessGoal
    
    ProcessGoal --> FindSkill{Skill Found?}
    
    FindSkill -->|No| Fallback[Create Fallback Result<br/>success=false]
    FindSkill -->|Yes| Execute[Execute Skill]
    
    Execute --> SkillError{Execution Error?}
    
    SkillError -->|Yes| ErrorResult[ActionResult<br/>success=false<br/>error message]
    SkillError -->|No| SuccessResult[ActionResult<br/>success=true<br/>output data]
    
    Fallback --> BuildResponse[Build Response]
    ErrorResult --> BuildResponse
    SuccessResult --> BuildResponse
    
    BuildResponse --> Return[HTTP 200 OK<br/>JSON Response]
    
    style E1 fill:#ffcdd2
    style Fallback fill:#fff9c4
    style ErrorResult fill:#ffccbc
    style SuccessResult fill:#c8e6c9
    style Return fill:#b2ebf2
```

### Skill-Specific Error Handling

#### CalculatorSkill
```java
// Division by zero
input: "calculate: 5 / 0"
output: ActionResult(success=true, output="NaN")

// Invalid expression
input: "calculate: abc"
output: ActionResult(success=false, output="No simple expression found")

// Parse error
input: "calculate: 2 ++ 3"
output: ActionResult(success=false, output="Error evaluating expression")
```

#### SummarizeSkill
```java
// Empty payload
input: "summarize:"
output: ActionResult(success=false, output="No text to summarize")

// Valid but minimal
input: "summarize: Hi"
output: ActionResult(success=true, output="Hi")
```

#### MockSearchSkill
```java
// Always succeeds with mock data
input: "search: [any query]"
output: ActionResult(success=true, output="Mock search results...")
```

---

## State Diagram

### AgentService State Flow

```mermaid
stateDiagram-v2
    [*] --> Initialized : Application Start
    Initialized --> Ready : Skills Injected
    Ready --> ProcessingRequest : executeGoal() called
    ProcessingRequest --> CheckingSkill : Iterate skills
    CheckingSkill --> SkillMatched : canHandle() = true
    CheckingSkill --> CheckingSkill : canHandle() = false
    CheckingSkill --> NoSkillFound : All skills checked
    
    SkillMatched --> ExecutingSkill : execute() called
    ExecutingSkill --> Success : Returns ActionResult
    ExecutingSkill --> Error : Exception thrown
    
    Success --> Ready : Return trace
    Error --> Ready : Return error result
    NoSkillFound --> Ready : Return fallback
    
    Ready --> [*] : Application Shutdown
```

---

## Performance Specifications

### Expected Response Times
| Operation | Target | Notes |
|-----------|--------|-------|
| Calculator Skill | < 10ms | Simple arithmetic |
| Search Skill | < 50ms | Mock data generation |
| Summarize Skill | < 20ms | String manipulation |
| Overall Request | < 100ms | Including network overhead |

### Scalability Metrics
- **Concurrent Requests**: 100+ (limited by Tomcat default thread pool)
- **Memory Footprint**: ~200MB (typical Spring Boot app)
- **Skill Limit**: No hard limit, constrained by memory

---

## Testing Specification

### Test Coverage

```mermaid
graph TB
    Tests[Test Suite]
    
    Tests --> Unit[Unit Tests]
    Tests --> Integration[Integration Tests]
    
    Unit --> T1[AgentServiceTest]
    
    T1 --> T1a[testSummarizeSkill]
    T1 --> T1b[testCalculatorSkill]
    T1 --> T1c[testSearchSkill]
    
    Integration --> T2[End-to-End API Tests]
    Integration --> T3[Spring Context Tests]
    
    style Tests fill:#e3f2fd
    style Unit fill:#e8f5e9
    style Integration fill:#fff3e0
```

### Sample Test Cases

#### CalculatorSkill Tests
```java
@Test
void testCalculatorSkill() {
    List<ActionResult> trace = agentService.executeGoal("calculate: 5 + 3");
    assertEquals("CalculatorSkill", trace.get(0).getSkillName());
    assertEquals("8.0", trace.get(0).getOutput());
    assertTrue(trace.get(0).isSuccess());
}
```

#### SummarizeSkill Tests
```java
@Test
void testSummarizeSkill() {
    List<ActionResult> trace = agentService.executeGoal(
        "summarize: The quick brown fox. Second sentence.");
    assertEquals("SummarizeSkill", trace.get(0).getSkillName());
    assertEquals("The quick brown fox.", trace.get(0).getOutput());
}
```

#### MockSearchSkill Tests
```java
@Test
void testSearchSkill() {
    List<ActionResult> trace = agentService.executeGoal("search: Java docs");
    assertEquals("MockSearchSkill", trace.get(0).getSkillName());
    assertTrue(trace.get(0).getOutput().contains("Mock search results"));
}
```

---

## Extension Points

### Adding New Skills

```mermaid
flowchart LR
    A[Create Skill Class] --> B[Implement Skill Interface]
    B --> C[Add @Component Annotation]
    C --> D[Implement canHandle]
    D --> E[Implement execute]
    E --> F[Spring Auto-discovers]
    F --> G[Skill Available]
    
    style A fill:#e3f2fd
    style C fill:#fff3e0
    style F fill:#e8f5e9
    style G fill:#c8e6c9
```

### Example: Adding a TranslateSkill

```java
@Component
public class TranslateSkill implements Skill {
    @Override
    public boolean canHandle(String goal) {
        return goal != null && 
               goal.toLowerCase().contains("translate");
    }
    
    @Override
    public ActionResult execute(String goal) {
        // Implementation
        return new ActionResult(true, 
                               "TranslateSkill", 
                               "Translated text");
    }
}
```

---

## Configuration Specification

### Application Properties
```properties
# Server Configuration
server.port=8080

# Logging (future)
logging.level.com.example.agentdemo=DEBUG

# Actuator (future)
management.endpoints.web.exposure.include=health,info,metrics
```

### Build Configuration
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

---

## Monitoring and Observability (Future)

```mermaid
graph LR
    App[Application]
    
    App --> Metrics[Metrics<br/>Micrometer]
    App --> Logs[Logs<br/>SLF4J]
    App --> Traces[Traces<br/>OpenTelemetry]
    
    Metrics --> Prometheus[Prometheus]
    Logs --> ELK[ELK Stack]
    Traces --> Jaeger[Jaeger]
    
    Prometheus --> Grafana[Grafana Dashboard]
    
    style App fill:#e3f2fd
    style Metrics fill:#fff3e0
    style Logs fill:#f3e5f5
    style Traces fill:#e8f5e9
```

---

*Generated: November 8, 2025*  
*Project: Spring AI Agent Demo v0.0.1-SNAPSHOT*  
*Technical Specification Version: 1.0*
