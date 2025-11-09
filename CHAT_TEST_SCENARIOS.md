# Interactive Chat Test Scenarios

This document outlines test scenarios for the interactive chat mode of the Spring AI Agent.

## Test Categories

### 1. Calculator Skill Tests

#### Scenario 1.1: Basic Addition
```
Input: calculate: 10 + 5
Expected Output: [CalculatorSkill] 15.0
```

#### Scenario 1.2: Subtraction
```
Input: calculate: 100 - 25
Expected Output: [CalculatorSkill] 75.0
```

#### Scenario 1.3: Multiplication
```
Input: calculate: 7 * 8
Expected Output: [CalculatorSkill] 56.0
```

#### Scenario 1.4: Division
```
Input: calculate: 144 / 12
Expected Output: [CalculatorSkill] 12.0
```

#### Scenario 1.5: Decimal Operations
```
Input: calculate: 3.5 * 2.0
Expected Output: [CalculatorSkill] 7.0
```

#### Scenario 1.6: Alternative Keywords
```
Input: compute: 20 + 30
Expected Output: [CalculatorSkill] 50.0
```

### 2. Search Skill Tests

#### Scenario 2.1: Basic Search Query
```
Input: search: Spring Boot documentation
Expected Output: [MockSearchSkill] Mock search results for 'Spring Boot documentation':
1) Example result A - short description
2) Example result B - short description
3) Example result C - short description
```

#### Scenario 2.2: Alternative Search Keywords
```
Input: find: Java 21 features
Expected Output: [MockSearchSkill] Mock search results for 'Java 21 features': ...
```

#### Scenario 2.3: Lookup Keyword
```
Input: lookup: MCP protocol
Expected Output: [MockSearchSkill] Mock search results for 'MCP protocol': ...
```

### 3. Summarize Skill Tests

#### Scenario 3.1: Short Text Summarization
```
Input: summarize: The quick brown fox jumps over the lazy dog.
Expected Output: [SummarizeSkill] The quick brown fox jumps over the lazy dog.
```

#### Scenario 3.2: Long Text Summarization
```
Input: summarize: The quick brown fox jumps over the lazy dog. It is a classic pangram containing all letters of the alphabet. This sentence has been used for typing practice and font testing for many years.
Expected Output: [SummarizeSkill] The quick brown fox jumps over the lazy dog. It is a classic pangram containing all letters of the alphabet.
```

#### Scenario 3.3: Alternative Keywords
```
Input: tldr: Spring Boot is a framework that makes it easy to create stand-alone, production-grade Spring applications.
Expected Output: [SummarizeSkill] Spring Boot is a framework that makes it easy to create stand-alone, production-grade Spring applications.
```

### 4. Osquery MCP Skill Tests (Requires osquery MCP server)

#### Scenario 4.1: System Info Query
```
Input: show system info
Expected Output: [OsqueryMCPSkill] [
  {
    "computer_name": "gpwin10",
    "cpu_brand": "Intel(R) Core(TM) i7-8650U CPU @ 1.90GHz",
    "cpu_logical_cores": "8",
    "cpu_physical_cores": "4",
    "cpu_type": "x86_64",
    "hostname": "gpwin10.",
    ...
  }
]
```

#### Scenario 4.2: Get Hostname (Natural Language)
```
Input: what is the hostname
Expected Output: [OsqueryMCPSkill] [
  {
    "hostname": "gpwin10."
  }
]
```

#### Scenario 4.3: Get Hostname (Simple Keyword)
```
Input: hostname
Expected Output: [OsqueryMCPSkill] [
  {
    "hostname": "gpwin10."
  }
]
```

#### Scenario 4.4: List Users
```
Input: list users
Expected Output: [OsqueryMCPSkill] [
  {
    "shell": "/bin/bash",
    "uid": "0",
    "username": "root"
  },
  {
    "shell": "/usr/sbin/nologin",
    "uid": "1",
    "username": "daemon"
  },
  ...
]
```

#### Scenario 4.5: List Running Processes
```
Input: running process
Expected Output: [OsqueryMCPSkill] <process list from osquery>
```

#### Scenario 4.6: Network Connections
```
Input: network connection
Expected Output: [OsqueryMCPSkill] <network connections from osquery>
```

#### Scenario 4.7: What is the System
```
Input: what is the system
Expected Output: [OsqueryMCPSkill] <system information>
```

### 5. Error Handling Tests

#### Scenario 5.1: Empty Input
```
Input: (empty/whitespace only)
Expected Behavior: Prompt again without error
```

#### Scenario 5.2: Unrecognized Command
```
Input: foobar: something random
Expected Output: [ERROR] No skill found to handle goal: foobar: something random
```

#### Scenario 5.3: Invalid Calculator Expression
```
Input: calculate: abc + def
Expected Output: [ERROR] or [CalculatorSkill] with error message
```

### 6. Exit Commands Tests

#### Scenario 6.1: Exit Command (lowercase)
```
Input: exit
Expected Behavior: Application terminates with "Goodbye!" message
```

#### Scenario 6.2: Exit Command (uppercase)
```
Input: EXIT
Expected Behavior: Application terminates with "Goodbye!" message
```

#### Scenario 6.3: Quit Command (lowercase)
```
Input: quit
Expected Behavior: Application terminates with "Goodbye!" message
```

#### Scenario 6.4: Quit Command (uppercase)
```
Input: QUIT
Expected Behavior: Application terminates with "Goodbye!" message
```

### 7. Multi-Step Conversation Tests

#### Scenario 7.1: Sequential Different Skills
```
Step 1: calculate: 5 + 5
Expected: [CalculatorSkill] 10.0

Step 2: search: Spring AI
Expected: [MockSearchSkill] Mock search results for 'Spring AI': ...

Step 3: summarize: This is a test.
Expected: [SummarizeSkill] This is a test.
```

#### Scenario 7.2: Multiple Calculator Operations
```
Step 1: calculate: 10 * 2
Expected: [CalculatorSkill] 20.0

Step 2: calculate: 20 + 5
Expected: [CalculatorSkill] 25.0

Step 3: calculate: 25 / 5
Expected: [CalculatorSkill] 5.0
```

### 8. Edge Cases

#### Scenario 8.1: Very Long Input
```
Input: summarize: [Very long text with 500+ characters]
Expected: [SummarizeSkill] [First 200 characters or summary]
```

#### Scenario 8.2: Special Characters
```
Input: search: C++ programming & design
Expected: [MockSearchSkill] Mock search results for 'C++ programming & design': ...
```

#### Scenario 8.3: Unicode Characters
```
Input: search: 中文 Japanese 日本語
Expected: [MockSearchSkill] Mock search results for '中文 Japanese 日本語': ...
```

## Manual Testing Checklist

- [ ] Application starts with `--chat` flag
- [ ] Application starts with `-c` flag
- [ ] Welcome banner displays correctly
- [ ] Prompt "You: " appears
- [ ] Calculator skill processes all operations correctly
- [ ] Search skill returns mock results
- [ ] Summarize skill truncates text appropriately
- [ ] Osquery skill connects to MCP server (if available)
- [ ] Error messages display for unrecognized commands
- [ ] Empty input is handled gracefully
- [ ] Exit command terminates application
- [ ] Quit command terminates application
- [ ] Multiple sequential commands work correctly
- [ ] Special characters are handled correctly

## Automated Testing

See `InteractiveChatTest.java` for automated unit tests covering these scenarios.

## Performance Benchmarks

Expected response times:
- Calculator operations: < 10ms
- Mock search: < 50ms
- Summarize: < 100ms
- Osquery MCP: < 500ms (depends on query complexity)

## Known Limitations

1. Interactive mode does not support multi-line input
2. No command history or readline support (use `rlwrap` wrapper if needed)
3. No auto-completion
4. Ctrl+C terminates immediately without cleanup
