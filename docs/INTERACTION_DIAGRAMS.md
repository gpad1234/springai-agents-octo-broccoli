# Spring AI Agent Demo - Interaction Diagrams & Workflows

## Table of Contents
1. [Request Processing Workflows](#request-processing-workflows)
2. [Skill Execution Patterns](#skill-execution-patterns)
3. [Spring Bean Lifecycle](#spring-bean-lifecycle)
4. [Communication Patterns](#communication-patterns)
5. [Deployment Scenarios](#deployment-scenarios)

---

## Request Processing Workflows

### 1. Happy Path - Successful Execution

```mermaid
graph TD
    Start([HTTP Request Arrives]) --> Parse[Parse JSON Body]
    Parse --> Extract[Extract Goal String]
    Extract --> Service[Call AgentService]
    
    Service --> Loop{For Each Skill}
    Loop --> Check[skill.canHandle?]
    
    Check -->|false| Next[Next Skill]
    Next --> Loop
    
    Check -->|true| Execute[skill.execute]
    Execute --> Result[Create ActionResult]
    Result --> Trace[Add to Trace List]
    Trace --> Response[Build Response Map]
    Response --> JSON[Serialize to JSON]
    JSON --> End([Return HTTP 200])
    
    style Start fill:#e3f2fd
    style Execute fill:#e8f5e9
    style Result fill:#fce4ec
    style End fill:#c8e6c9
```

### 2. Alternative Path - No Skill Match

```mermaid
graph TD
    Start([HTTP Request]) --> Parse[Parse Request]
    Parse --> Service[AgentService.executeGoal]
    
    Service --> S1{Check Skill 1}
    S1 -->|canHandle=false| S2{Check Skill 2}
    S2 -->|canHandle=false| S3{Check Skill 3}
    S3 -->|canHandle=false| Fallback[Create Fallback Result]
    
    Fallback --> FB[ActionResult:<br/>success=false<br/>skillName='none'<br/>output='No skill found']
    FB --> Response[Build Response]
    Response --> End([Return HTTP 200])
    
    style Start fill:#e3f2fd
    style Fallback fill:#fff9c4
    style FB fill:#ffccbc
    style End fill:#b2ebf2
```

### 3. Error Path - Skill Execution Exception

```mermaid
graph TD
    Start([Request]) --> Service[AgentService]
    Service --> Match{Skill Matched}
    Match -->|Yes| Execute[Execute Skill]
    
    Execute --> Try{Try-Catch}
    Try -->|Exception| Catch[Catch Block]
    Try -->|Success| Success[ActionResult:<br/>success=true]
    
    Catch --> ErrorResult[ActionResult:<br/>success=false<br/>error message]
    
    Success --> Response[Build Response]
    ErrorResult --> Response
    Response --> End([HTTP 200])
    
    style Execute fill:#fff3e0
    style Catch fill:#ffccbc
    style ErrorResult fill:#ffcdd2
    style Success fill:#c8e6c9
```

---

## Skill Execution Patterns

### CalculatorSkill Workflow

```mermaid
flowchart TD
    Input[Goal: 'calculate: 10 + 5'] --> Check{Contains 'calculate'<br/>or math operators?}
    
    Check -->|No| Return1[ActionResult:<br/>canHandle=false]
    Check -->|Yes| Extract[Extract Expression]
    
    Extract --> Split[Split at ':' if present]
    Split --> Regex[Apply Regex Pattern:<br/>'number operator number']
    
    Regex --> Match{Regex Match?}
    Match -->|No| Error1[ActionResult:<br/>success=false<br/>'No simple expression found']
    
    Match -->|Yes| Parse[Parse:<br/>a=10, op=+, b=5]
    Parse --> Operator{Switch on Operator}
    
    Operator -->|+| Add[result = a + b]
    Operator -->|-| Sub[result = a - b]
    Operator -->|*| Mul[result = a * b]
    Operator -->|/| Div{b == 0?}
    
    Div -->|Yes| NaN[result = NaN]
    Div -->|No| DivCalc[result = a / b]
    
    Add --> Success[ActionResult:<br/>success=true<br/>output='15.0']
    Sub --> Success
    Mul --> Success
    NaN --> Success
    DivCalc --> Success
    
    Success --> End([Return])
    Error1 --> End
    Return1 --> End
    
    style Input fill:#e3f2fd
    style Parse fill:#fff3e0
    style Success fill:#c8e6c9
    style Error1 fill:#ffccbc
    style End fill:#b2ebf2
```

### MockSearchSkill Workflow

```mermaid
flowchart TD
    Input[Goal: 'search: Java tutorials'] --> Check{Contains 'search'<br/>or 'find' or 'lookup'?}
    
    Check -->|No| Return1[canHandle=false]
    Check -->|Yes| Extract[Extract Query]
    
    Extract --> Split{Contains ':'?}
    Split -->|Yes| AfterColon[query = text after ':']
    Split -->|No| Full[query = full goal]
    
    AfterColon --> Generate[Generate Mock Results]
    Full --> Generate
    
    Generate --> Format[Format as:<br/>1) Result A<br/>2) Result B<br/>3) Result C]
    
    Format --> Success[ActionResult:<br/>success=true<br/>skillName='MockSearchSkill'<br/>output=formatted results]
    
    Success --> End([Return])
    Return1 --> End
    
    style Input fill:#e3f2fd
    style Generate fill:#e8f5e9
    style Success fill:#c8e6c9
    style End fill:#b2ebf2
```

### SummarizeSkill Workflow

```mermaid
flowchart TD
    Input[Goal: 'summarize: Text...'] --> Check{Contains 'summarize'<br/>or 'summary'?}
    
    Check -->|No| Return1[canHandle=false]
    Check -->|Yes| Extract[Extract Payload]
    
    Extract --> Split{Contains ':'?}
    Split -->|Yes| AfterColon[payload = text after ':']
    Split -->|No| Full[payload = full goal]
    
    AfterColon --> Empty{payload.isEmpty?}
    Full --> Empty
    
    Empty -->|Yes| Error[ActionResult:<br/>success=false<br/>'No text to summarize']
    
    Empty -->|No| FindPeriod{Find first '.'<br/>within 200 chars?}
    
    FindPeriod -->|Found| FirstSentence[summary = text up to period]
    FindPeriod -->|Not Found| CheckLength{Text length > 120?}
    
    CheckLength -->|Yes| Truncate[summary = first 120 chars + '...']
    CheckLength -->|No| FullText[summary = full text]
    
    FirstSentence --> Success[ActionResult:<br/>success=true<br/>output=summary]
    Truncate --> Success
    FullText --> Success
    
    Success --> End([Return])
    Error --> End
    Return1 --> End
    
    style Input fill:#e3f2fd
    style FindPeriod fill:#fff3e0
    style Success fill:#c8e6c9
    style Error fill:#ffccbc
    style End fill:#b2ebf2
```

---

## Spring Bean Lifecycle

### Application Startup Sequence

```mermaid
sequenceDiagram
    participant Main as AgentDemoApplication
    participant Spring as Spring Container
    participant Scanner as Component Scanner
    participant Factory as Bean Factory
    participant Controller as AgentController
    participant Service as AgentService
    participant Skills as Skill Beans
    
    Main->>Spring: SpringApplication.run()
    activate Spring
    
    Spring->>Scanner: Scan @SpringBootApplication package
    activate Scanner
    
    Scanner->>Scanner: Find @Component classes
    Scanner->>Scanner: Find @Service classes
    Scanner->>Scanner: Find @RestController classes
    
    Scanner-->>Spring: Component definitions
    deactivate Scanner
    
    Spring->>Factory: Create beans
    activate Factory
    
    Factory->>Skills: Instantiate CalculatorSkill
    Factory->>Skills: Instantiate MockSearchSkill
    Factory->>Skills: Instantiate SummarizeSkill
    Note over Factory,Skills: All @Component beans created
    
    Factory->>Service: Instantiate AgentService
    Note over Factory,Service: Inject List<Skill>
    
    Factory->>Controller: Instantiate AgentController
    Note over Factory,Controller: Inject AgentService
    
    Factory-->>Spring: All beans ready
    deactivate Factory
    
    Spring->>Spring: Start embedded Tomcat
    Spring->>Spring: Register REST endpoints
    
    Spring-->>Main: Application started
    deactivate Spring
    
    Note over Main,Skills: Application Ready<br/>Listening on port 8080
```

### Dependency Injection Flow

```mermaid
graph TB
    subgraph "Spring IoC Container"
        BeanDef[Bean Definitions]
        
        subgraph "Skill Beans"
            B1[CalculatorSkill<br/>@Component]
            B2[MockSearchSkill<br/>@Component]
            B3[SummarizeSkill<br/>@Component]
        end
        
        B4[AgentService<br/>@Service]
        B5[AgentController<br/>@RestController]
        
        BeanDef --> B1
        BeanDef --> B2
        BeanDef --> B3
        BeanDef --> B4
        BeanDef --> B5
    end
    
    B1 -->|Auto-collected| Collection[List&lt;Skill&gt;]
    B2 -->|Auto-collected| Collection
    B3 -->|Auto-collected| Collection
    
    Collection -->|Constructor Injection| B4
    B4 -->|Constructor Injection| B5
    
    Request[HTTP Request] -->|Routes to| B5
    
    style BeanDef fill:#e3f2fd
    style B1 fill:#e8f5e9
    style B2 fill:#e8f5e9
    style B3 fill:#e8f5e9
    style B4 fill:#fff4e1
    style B5 fill:#e1f5ff
    style Collection fill:#fff3e0
```

---

## Communication Patterns

### HTTP Request/Response Cycle

```mermaid
sequenceDiagram
    participant Client
    participant Tomcat as Embedded Tomcat
    participant Dispatcher as DispatcherServlet
    participant Handler as HandlerMapping
    participant Controller as AgentController
    participant Service as AgentService
    
    Client->>Tomcat: HTTP POST Request<br/>Content-Type: application/json
    activate Tomcat
    
    Tomcat->>Dispatcher: Forward request
    activate Dispatcher
    
    Dispatcher->>Handler: Find handler for<br/>/api/agent/execute
    activate Handler
    Handler-->>Dispatcher: AgentController.execute()
    deactivate Handler
    
    Dispatcher->>Controller: Invoke execute(body)
    activate Controller
    
    Controller->>Controller: Extract goal from Map
    Controller->>Service: executeGoal(goal)
    activate Service
    
    Service->>Service: Find and execute skill
    Service-->>Controller: List<ActionResult>
    deactivate Service
    
    Controller->>Controller: Build response Map
    Controller-->>Dispatcher: ResponseEntity<Map>
    deactivate Controller
    
    Dispatcher->>Dispatcher: Serialize to JSON<br/>(Jackson)
    Dispatcher-->>Tomcat: HTTP Response
    deactivate Dispatcher
    
    Tomcat-->>Client: HTTP 200 OK<br/>application/json
    deactivate Tomcat
```

### Internal Component Communication

```mermaid
graph LR
    subgraph "Web Layer"
        A[AgentController]
    end
    
    subgraph "Service Layer"
        B[AgentService]
    end
    
    subgraph "Execution Layer"
        C1[CalculatorSkill]
        C2[MockSearchSkill]
        C3[SummarizeSkill]
    end
    
    subgraph "Data Layer"
        D[ActionResult]
    end
    
    A -->|String goal| B
    B -->|List<ActionResult>| A
    
    B -->|canHandle query| C1
    B -->|canHandle query| C2
    B -->|canHandle query| C3
    
    C1 -->|ActionResult| B
    C2 -->|ActionResult| B
    C3 -->|ActionResult| B
    
    C1 -.creates.- D
    C2 -.creates.- D
    C3 -.creates.- D
    
    style A fill:#e1f5ff
    style B fill:#fff4e1
    style C1 fill:#e8f5e9
    style C2 fill:#e8f5e9
    style C3 fill:#e8f5e9
    style D fill:#fce4ec
```

---

## Deployment Scenarios

### Local Development Deployment

```mermaid
graph TB
    subgraph "Developer Machine"
        IDE[IDE<br/>VS Code / IntelliJ]
        Maven[Maven Build]
        JVM21[JVM 21]
        
        IDE --> Maven
        Maven --> JAR[springai-agent-demo.jar]
        JAR --> JVM21
        
        subgraph "Running Application"
            JVM21 --> SpringBoot[Spring Boot App]
            SpringBoot --> Tomcat[Embedded Tomcat:8080]
        end
    end
    
    Dev[Developer] -->|mvn spring-boot:run| IDE
    Browser[Browser] -->|http://localhost:8080| Tomcat
    
    style IDE fill:#e3f2fd
    style JVM21 fill:#fff3e0
    style SpringBoot fill:#e8f5e9
    style Tomcat fill:#c8e6c9
```

### Docker Containerized Deployment

```mermaid
graph TB
    subgraph "Container"
        Base[Base Image<br/>openjdk:21-slim]
        App[Application JAR]
        JVM[JVM Process]
        
        Base --> App
        App --> JVM
        
        subgraph "Spring Boot"
            JVM --> SB[Spring Application]
            SB --> Port[Expose 8080]
        end
    end
    
    Docker[Docker Engine] -->|runs| Container
    Client[External Client] -->|http://host:8080| Port
    
    style Base fill:#e3f2fd
    style JVM fill:#fff3e0
    style SB fill:#e8f5e9
    style Port fill:#c8e6c9
```

#### Sample Dockerfile

```dockerfile
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY target/springai-agent-demo-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xmx512m -Xms256m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### Cloud Deployment (Kubernetes)

```mermaid
graph TB
    subgraph "Kubernetes Cluster"
        subgraph "Namespace: default"
            Service[Service<br/>Type: LoadBalancer]
            
            subgraph "Deployment"
                Pod1[Pod 1<br/>Agent App]
                Pod2[Pod 2<br/>Agent App]
                Pod3[Pod 3<br/>Agent App]
            end
            
            Service --> Pod1
            Service --> Pod2
            Service --> Pod3
        end
        
        ConfigMap[ConfigMap<br/>application.properties]
        
        Pod1 -.uses.- ConfigMap
        Pod2 -.uses.- ConfigMap
        Pod3 -.uses.- ConfigMap
    end
    
    LB[Load Balancer] --> Service
    Users[Users] --> LB
    
    style Service fill:#e1f5ff
    style Pod1 fill:#e8f5e9
    style Pod2 fill:#e8f5e9
    style Pod3 fill:#e8f5e9
    style ConfigMap fill:#fff3e0
    style LB fill:#ffebee
```

#### Sample Kubernetes Manifests

**Deployment:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: agent-demo
spec:
  replicas: 3
  selector:
    matchLabels:
      app: agent-demo
  template:
    metadata:
      labels:
        app: agent-demo
    spec:
      containers:
      - name: agent-demo
        image: agent-demo:latest
        ports:
        - containerPort: 8080
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
```

**Service:**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: agent-demo-service
spec:
  type: LoadBalancer
  selector:
    app: agent-demo
  ports:
  - port: 80
    targetPort: 8080
```

---

## Complete End-to-End Flow

### Multi-Step Interaction (Future Enhancement)

```mermaid
sequenceDiagram
    actor User
    participant API
    participant Controller
    participant Service
    participant Calc as CalculatorSkill
    participant Search as SearchSkill
    
    Note over User,Search: Future: Multi-step execution
    
    User->>API: "Find the price of product X<br/>and calculate 15% discount"
    API->>Controller: POST /api/agent/execute
    
    Controller->>Service: executeGoal(complex goal)
    
    Service->>Service: Parse into sub-goals:<br/>1. Search for price<br/>2. Calculate discount
    
    Service->>Search: execute("search: product X")
    Search-->>Service: ActionResult(price=$100)
    
    Service->>Calc: execute("calculate: 100 * 0.15")
    Calc-->>Service: ActionResult(discount=$15)
    
    Service->>Service: Combine results
    Service-->>Controller: List<ActionResult>
    
    Controller-->>API: Combined response
    API-->>User: "Product X: $100<br/>15% discount: $15<br/>Final: $85"
    
    Note over User,Search: Current: Single skill execution only
```

---

## Performance & Monitoring

### Request Timeline

```mermaid
gantt
    title HTTP Request Processing Timeline
    dateFormat X
    axisFormat %L ms
    
    section Network
    Request Arrival           :0, 5
    Response Sent            :95, 100
    
    section Web Layer
    Parse JSON               :5, 10
    Controller Execution     :10, 15
    Response Building        :90, 95
    
    section Service Layer
    AgentService.executeGoal :15, 85
    Skill Iteration          :15, 25
    
    section Execution Layer
    Skill Execution          :25, 80
    Business Logic           :25, 75
    Result Creation          :75, 80
    
    section Trace Building
    Add to Trace             :80, 85
```

### Metrics Collection Points (Future)

```mermaid
graph TB
    Request[Incoming Request] --> M1[Metric: request.count]
    Request --> Timer1[Timer: request.duration]
    
    Controller[Controller] --> M2[Metric: controller.invocations]
    
    Service[AgentService] --> M3[Metric: skill.matches]
    Service --> M4[Metric: skill.executions]
    Service --> Timer2[Timer: skill.execution.time]
    
    Skills[Individual Skills] --> M5[Metric: skill.success.rate]
    Skills --> M6[Metric: skill.error.count]
    
    Response[Response] --> Timer1
    Response --> M7[Metric: response.status]
    
    M1 --> Prometheus[Prometheus]
    M2 --> Prometheus
    M3 --> Prometheus
    M4 --> Prometheus
    M5 --> Prometheus
    M6 --> Prometheus
    M7 --> Prometheus
    Timer1 --> Prometheus
    Timer2 --> Prometheus
    
    Prometheus --> Grafana[Grafana Dashboard]
    
    style Request fill:#e3f2fd
    style Prometheus fill:#fff3e0
    style Grafana fill:#e8f5e9
```

---

## Security Flow (Future Enhancement)

### Authentication & Authorization Flow

```mermaid
sequenceDiagram
    participant Client
    participant Gateway as API Gateway
    participant Auth as Auth Service
    participant Controller
    participant Service
    
    Client->>Gateway: POST /api/agent/execute<br/>Authorization: Bearer <token>
    activate Gateway
    
    Gateway->>Auth: Validate token
    activate Auth
    Auth-->>Gateway: Token valid + user info
    deactivate Auth
    
    Gateway->>Controller: Forward request<br/>+ user context
    activate Controller
    
    Controller->>Controller: Check permissions
    Controller->>Service: executeGoal(goal)
    activate Service
    Service-->>Controller: Results
    deactivate Service
    
    Controller-->>Gateway: Response
    deactivate Controller
    
    Gateway-->>Client: HTTP 200 OK
    deactivate Gateway
```

---

## Development Workflow

### Local Development Cycle

```mermaid
flowchart LR
    A[Write Code] --> B[Save Files]
    B --> C{Spring DevTools?}
    
    C -->|Yes| D[Auto Restart]
    C -->|No| E[Manual Restart]
    
    D --> F[Test Endpoint]
    E --> F
    
    F --> G{Works?}
    
    G -->|No| H[Debug]
    G -->|Yes| I[Commit]
    
    H --> A
    I --> J[Push to Repo]
    
    style A fill:#e3f2fd
    style D fill:#e8f5e9
    style F fill:#fff3e0
    style I fill:#c8e6c9
    style J fill:#b2ebf2
```

### CI/CD Pipeline (Future)

```mermaid
graph LR
    Commit[Git Commit] --> Push[Push to GitHub]
    
    Push --> CI{CI Pipeline}
    
    CI --> Build[Maven Build]
    Build --> Test[Run Tests]
    Test --> Package[Package JAR]
    
    Package --> Docker[Build Docker Image]
    Docker --> Registry[Push to Registry]
    
    Registry --> CD{CD Pipeline}
    
    CD --> Deploy[Deploy to K8s]
    Deploy --> Verify[Health Check]
    
    Verify --> Success[Deployment Complete]
    
    style Commit fill:#e3f2fd
    style Build fill:#fff3e0
    style Test fill:#e8f5e9
    style Docker fill:#e1f5ff
    style Success fill:#c8e6c9
```

---

*Generated: November 8, 2025*  
*Project: Spring AI Agent Demo v0.0.1-SNAPSHOT*  
*Interaction Diagrams Version: 1.0*
