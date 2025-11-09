# Spring AI Agent Demo - Visual Summary

A quick visual reference guide for the Spring AI Agent Demo application.

---

## 🎯 System At-A-Glance

```mermaid
mindmap
  root((Spring AI<br/>Agent Demo))
    Presentation
      REST API
      AgentController
      JSON Request/Response
    Business Logic
      AgentService
      Skill Selection
      Execution Orchestration
    Skills
      CalculatorSkill
      MockSearchSkill
      SummarizeSkill
      Extensible
    Technology
      Java 21
      Spring Boot 3.2.11
      Maven
      JUnit 5
```

---

## 🔄 Request Flow Overview

```mermaid
graph LR
    A[Client] -->|HTTP POST| B[Controller]
    B -->|Execute Goal| C[AgentService]
    C -->|Route to| D{Select Skill}
    D -->|Match| E[Skill Execute]
    E -->|Result| F[ActionResult]
    F -->|Trace| B
    B -->|JSON| A
    
    style A fill:#e3f2fd
    style B fill:#e1f5ff
    style C fill:#fff4e1
    style D fill:#fff3e0
    style E fill:#e8f5e9
    style F fill:#fce4ec
```

---

## 🧩 Component Relationships

```mermaid
graph TB
    subgraph API["🌐 API Layer"]
        AC[AgentController]
    end
    
    subgraph Service["⚙️ Service Layer"]
        AS[AgentService]
    end
    
    subgraph Skills["🔧 Skills"]
        CS[CalculatorSkill]
        MS[MockSearchSkill]
        SS[SummarizeSkill]
    end
    
    subgraph Model["📦 Model"]
        AR[ActionResult]
    end
    
    AC --> AS
    AS --> CS
    AS --> MS
    AS --> SS
    CS --> AR
    MS --> AR
    SS --> AR
    
    style AC fill:#e1f5ff
    style AS fill:#fff4e1
    style CS fill:#e8f5e9
    style MS fill:#e8f5e9
    style SS fill:#e8f5e9
    style AR fill:#fce4ec
```

---

## 📊 Technology Stack

```mermaid
graph TB
    subgraph Platform["Platform"]
        Java[Java 21 LTS]
    end
    
    subgraph Framework["Framework"]
        SB[Spring Boot 3.2.11]
        SW[Spring Web MVC]
    end
    
    subgraph Build["Build & Test"]
        Maven[Maven 3.x]
        JUnit[JUnit 5]
    end
    
    subgraph Server["Server"]
        Tomcat[Embedded Tomcat]
    end
    
    Java --> SB
    SB --> SW
    SB --> Tomcat
    Maven --> SB
    JUnit --> SB
    
    style Java fill:#f44336
    style SB fill:#4caf50
    style Maven fill:#2196f3
    style JUnit fill:#ff9800
    style Tomcat fill:#9c27b0
```

---

## 🎨 Skill Capabilities Matrix

| Skill | Trigger Keywords | Input Example | Output Type |
|-------|-----------------|---------------|-------------|
| 🧮 **CalculatorSkill** | calculate, compute, sum, math operators | `"calculate: 10 + 5"` | Numeric result |
| 🔍 **MockSearchSkill** | search, find, lookup | `"search: Spring Boot"` | Mock results list |
| 📝 **SummarizeSkill** | summarize, summary | `"summarize: Long text..."` | Shortened text |

---

## 🔀 Execution Patterns

### Pattern 1: Direct Skill Match

```mermaid
sequenceDiagram
    Client->>AgentService: Goal
    AgentService->>Skill1: canHandle?
    Skill1->>AgentService: true
    AgentService->>Skill1: execute
    Skill1->>AgentService: ActionResult
    AgentService->>Client: Response
```

### Pattern 2: Multiple Skill Check

```mermaid
sequenceDiagram
    Client->>AgentService: Goal
    AgentService->>Skill1: canHandle?
    Skill1->>AgentService: false
    AgentService->>Skill2: canHandle?
    Skill2->>AgentService: true
    AgentService->>Skill2: execute
    Skill2->>AgentService: ActionResult
    AgentService->>Client: Response
```

### Pattern 3: No Match Fallback

```mermaid
sequenceDiagram
    Client->>AgentService: Unknown Goal
    AgentService->>Skill1: canHandle?
    Skill1->>AgentService: false
    AgentService->>Skill2: canHandle?
    Skill2->>AgentService: false
    AgentService->>Skill3: canHandle?
    Skill3->>AgentService: false
    AgentService->>AgentService: Create Fallback
    AgentService->>Client: Error Response
```

---

## 📐 Architecture Layers

```mermaid
graph TD
    subgraph L1["Layer 1: Presentation"]
        direction LR
        REST[REST Controller]
        JSON[JSON Serialization]
    end
    
    subgraph L2["Layer 2: Business Logic"]
        direction LR
        Orchestration[AgentService Orchestration]
        Selection[Skill Selection Logic]
    end
    
    subgraph L3["Layer 3: Execution"]
        direction LR
        Calc[Calculator]
        Search[Search]
        Sum[Summarize]
    end
    
    subgraph L4["Layer 4: Domain Model"]
        direction LR
        Model[ActionResult]
        Interface[Skill Interface]
    end
    
    L1 --> L2
    L2 --> L3
    L3 --> L4
    
    style L1 fill:#e1f5ff
    style L2 fill:#fff4e1
    style L3 fill:#e8f5e9
    style L4 fill:#fce4ec
```

---

## 🚀 Deployment Options

```mermaid
graph TB
    Source[Source Code]
    
    Source --> Local[Local Development]
    Source --> Docker[Docker Container]
    Source --> K8s[Kubernetes]
    Source --> Cloud[Cloud Platform]
    
    Local --> IDE[IDE: mvn spring-boot:run]
    Docker --> Image[Docker Image]
    K8s --> Pods[Pod Replicas]
    Cloud --> PaaS[PaaS Services]
    
    style Source fill:#e3f2fd
    style Local fill:#c8e6c9
    style Docker fill:#bbdefb
    style K8s fill:#f0f4c3
    style Cloud fill:#ffe0b2
```

---

## 🔐 Request/Response Format

### Request Structure
```json
{
  "goal": "string - The task for the agent to execute"
}
```

### Response Structure
```json
{
  "goal": "string - Echo of original goal",
  "trace": [
    {
      "success": "boolean - Execution status",
      "skillName": "string - Skill that executed",
      "output": "string - Result data"
    }
  ],
  "finalOutput": "string - Last result in trace"
}
```

---

## 📈 Performance Characteristics

```mermaid
graph LR
    A[Request] -->|< 10ms| B[Controller]
    B -->|< 10ms| C[Service]
    C -->|< 50ms| D[Skill]
    D -->|< 10ms| E[Response]
    
    style A fill:#e3f2fd
    style B fill:#e1f5ff
    style C fill:#fff4e1
    style D fill:#e8f5e9
    style E fill:#c8e6c9
```

**Total Expected Response Time: < 100ms**

---

## 🔍 Key Design Patterns

```mermaid
graph TB
    DP[Design Patterns]
    
    DP --> Strategy[Strategy Pattern]
    DP --> DI[Dependency Injection]
    DP --> Chain[Chain of Responsibility]
    DP --> Template[Template Method]
    
    Strategy --> S1[Skills as Strategies]
    DI --> D1[Spring IoC Container]
    Chain --> C1[Skill Iteration]
    Template --> T1[canHandle + execute]
    
    style DP fill:#e3f2fd
    style Strategy fill:#c8e6c9
    style DI fill:#fff9c4
    style Chain fill:#ffccbc
    style Template fill:#d1c4e9
```

---

## 🧪 Test Coverage Overview

```mermaid
pie title Test Distribution
    "AgentService Tests" : 40
    "CalculatorSkill Tests" : 20
    "SearchSkill Tests" : 20
    "SummarizeSkill Tests" : 20
```

---

## 🎯 Extension Points

```mermaid
graph LR
    NewSkill[New Skill] -->|1. Implement| Interface[Skill Interface]
    Interface -->|2. Add| Component[@Component]
    Component -->|3. Auto-discover| Spring[Spring Container]
    Spring -->|4. Inject| Service[AgentService]
    Service -->|5. Available| Ready[Ready to Use]
    
    style NewSkill fill:#e3f2fd
    style Interface fill:#fff3e0
    style Component fill:#c8e6c9
    style Spring fill:#ffccbc
    style Ready fill:#d1c4e9
```

---

## 📊 Data Flow Summary

```mermaid
graph LR
    A[HTTP JSON] -->|Parse| B[Map Goal]
    B -->|String| C[AgentService]
    C -->|Iterate| D[Skills]
    D -->|Execute| E[ActionResult]
    E -->|Collect| F[List Trace]
    F -->|Serialize| G[JSON Response]
    
    style A fill:#e3f2fd
    style C fill:#fff4e1
    style D fill:#e8f5e9
    style E fill:#fce4ec
    style G fill:#c8e6c9
```

---

## 🛠️ Development Workflow

```mermaid
flowchart LR
    A[Code] --> B[Test]
    B --> C{Pass?}
    C -->|Yes| D[Commit]
    C -->|No| A
    D --> E[Build]
    E --> F[Deploy]
    
    style A fill:#e3f2fd
    style B fill:#fff3e0
    style C fill:#fff9c4
    style D fill:#c8e6c9
    style E fill:#bbdefb
    style F fill:#d1c4e9
```

---

## 📍 Project Structure Map

```
📁 springai_agent_getting_started
├── 📁 src
│   ├── 📁 main
│   │   ├── 📁 java/com/example/agentdemo
│   │   │   ├── 🎯 AgentDemoApplication.java
│   │   │   ├── 📁 agent
│   │   │   │   ├── ⚙️ AgentService.java
│   │   │   │   ├── 📋 Skill.java
│   │   │   │   └── 📁 skills
│   │   │   │       ├── 🧮 CalculatorSkill.java
│   │   │   │       ├── 🔍 MockSearchSkill.java
│   │   │   │       └── 📝 SummarizeSkill.java
│   │   │   ├── 📁 controller
│   │   │   │   └── 🌐 AgentController.java
│   │   │   └── 📁 model
│   │   │       └── 📦 ActionResult.java
│   │   └── 📁 resources
│   │       └── ⚙️ application.properties
│   └── 📁 test
│       └── 📁 java
│           └── 🧪 AgentServiceTest.java
├── 📁 docs
│   ├── 📄 README.md
│   ├── 📄 ARCHITECTURE.md
│   ├── 📄 TECHNICAL_SPEC.md
│   └── 📄 INTERACTION_DIAGRAMS.md
├── 📄 pom.xml
└── 📄 README.md
```

---

## 🎓 Quick Start Commands

```bash
# Build
mvn clean install

# Test
mvn test

# Run
mvn spring-boot:run

# Package
mvn package

# Run JAR
java -jar target/springai-agent-demo-0.0.1-SNAPSHOT.jar
```

---

## 🔗 Quick Links

| Resource | Location |
|----------|----------|
| **Main README** | [README.md](../README.md) |
| **Architecture** | [docs/ARCHITECTURE.md](ARCHITECTURE.md) |
| **Technical Spec** | [docs/TECHNICAL_SPEC.md](TECHNICAL_SPEC.md) |
| **Interactions** | [docs/INTERACTION_DIAGRAMS.md](INTERACTION_DIAGRAMS.md) |
| **Source Code** | [src/main/java/](../src/main/java/) |
| **Tests** | [src/test/java/](../src/test/java/) |

---

## 📞 API Quick Reference

```http
POST http://localhost:8080/api/agent/execute
Content-Type: application/json

{
  "goal": "calculate: 15 + 27"
}
```

**Response:**
```json
{
  "goal": "calculate: 15 + 27",
  "trace": [{"success": true, "skillName": "CalculatorSkill", "output": "42.0"}],
  "finalOutput": "42.0"
}
```

---

*Visual Summary Version: 1.0*  
*Last Updated: November 8, 2025*  
*Project Version: 0.0.1-SNAPSHOT*
