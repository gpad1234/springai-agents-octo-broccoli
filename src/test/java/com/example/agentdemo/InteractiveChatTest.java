package com.example.agentdemo;

import com.example.agentdemo.agent.AgentService;
import com.example.agentdemo.model.ActionResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for interactive chat scenarios.
 * These tests validate the agent's ability to handle various user inputs
 * as documented in CHAT_TEST_SCENARIOS.md
 */
@SpringBootTest
@DisplayName("Interactive Chat Mode Tests")
public class InteractiveChatTest {

    @Autowired
    private AgentService agentService;

    // ===============================
    // 1. Calculator Skill Tests
    // ===============================

    @Test
    @DisplayName("Scenario 1.1: Basic Addition")
    public void testBasicAddition() {
        List<ActionResult> results = agentService.executeGoal("calculate: 10 + 5");
        
        assertNotNull(results);
        assertFalse(results.isEmpty());
        
        ActionResult result = results.get(0);
        assertTrue(result.isSuccess(), "Calculator should succeed");
        assertEquals("CalculatorSkill", result.getSkillName());
        assertEquals("15.0", result.getOutput());
    }

    @Test
    @DisplayName("Scenario 1.2: Subtraction")
    public void testSubtraction() {
        List<ActionResult> results = agentService.executeGoal("calculate: 100 - 25");
        
        ActionResult result = results.get(0);
        assertTrue(result.isSuccess());
        assertEquals("CalculatorSkill", result.getSkillName());
        assertEquals("75.0", result.getOutput());
    }

    @Test
    @DisplayName("Scenario 1.3: Multiplication")
    public void testMultiplication() {
        List<ActionResult> results = agentService.executeGoal("calculate: 7 * 8");
        
        ActionResult result = results.get(0);
        assertTrue(result.isSuccess());
        assertEquals("CalculatorSkill", result.getSkillName());
        assertEquals("56.0", result.getOutput());
    }

    @Test
    @DisplayName("Scenario 1.4: Division")
    public void testDivision() {
        List<ActionResult> results = agentService.executeGoal("calculate: 144 / 12");
        
        ActionResult result = results.get(0);
        assertTrue(result.isSuccess());
        assertEquals("CalculatorSkill", result.getSkillName());
        assertEquals("12.0", result.getOutput());
    }

    @Test
    @DisplayName("Scenario 1.5: Decimal Operations")
    public void testDecimalOperations() {
        List<ActionResult> results = agentService.executeGoal("calculate: 3.5 * 2.0");
        
        ActionResult result = results.get(0);
        assertTrue(result.isSuccess());
        assertEquals("CalculatorSkill", result.getSkillName());
        assertEquals("7.0", result.getOutput());
    }

    @Test
    @DisplayName("Scenario 1.6: Alternative Keywords (compute)")
    public void testAlternativeCalculatorKeyword() {
        List<ActionResult> results = agentService.executeGoal("compute: 20 + 30");
        
        ActionResult result = results.get(0);
        assertTrue(result.isSuccess());
        assertEquals("CalculatorSkill", result.getSkillName());
        assertEquals("50.0", result.getOutput());
    }

    // ===============================
    // 2. Search Skill Tests
    // ===============================

    @Test
    @DisplayName("Scenario 2.1: Basic Search Query")
    public void testBasicSearch() {
        List<ActionResult> results = agentService.executeGoal("search: Spring Boot documentation");
        
        ActionResult result = results.get(0);
        assertTrue(result.isSuccess());
        assertEquals("MockSearchSkill", result.getSkillName());
        assertTrue(result.getOutput().contains("Mock search results for 'Spring Boot documentation'"));
        assertTrue(result.getOutput().contains("Example result A"));
    }

    @Test
    @DisplayName("Scenario 2.2: Alternative Search Keywords (find)")
    public void testFindKeyword() {
        List<ActionResult> results = agentService.executeGoal("find: Java 21 features");
        
        ActionResult result = results.get(0);
        assertTrue(result.isSuccess());
        assertEquals("MockSearchSkill", result.getSkillName());
        assertTrue(result.getOutput().contains("Mock search results for 'Java 21 features'"));
    }

    @Test
    @DisplayName("Scenario 2.3: Lookup Keyword")
    public void testLookupKeyword() {
        List<ActionResult> results = agentService.executeGoal("lookup: MCP protocol");
        
        ActionResult result = results.get(0);
        assertTrue(result.isSuccess());
        assertEquals("MockSearchSkill", result.getSkillName());
        assertTrue(result.getOutput().contains("Mock search results for 'MCP protocol'"));
    }

    // ===============================
    // 3. Summarize Skill Tests
    // ===============================

    @Test
    @DisplayName("Scenario 3.1: Short Text Summarization")
    public void testShortTextSummarization() {
        List<ActionResult> results = agentService.executeGoal(
            "summarize: The quick brown fox jumps over the lazy dog."
        );
        
        ActionResult result = results.get(0);
        assertTrue(result.isSuccess());
        assertEquals("SummarizeSkill", result.getSkillName());
        assertTrue(result.getOutput().contains("The quick brown fox"));
    }

    @Test
    @DisplayName("Scenario 3.2: Long Text Summarization")
    public void testLongTextSummarization() {
        String longText = "summarize: The quick brown fox jumps over the lazy dog. " +
                         "It is a classic pangram containing all letters of the alphabet. " +
                         "This sentence has been used for typing practice and font testing for many years.";
        
        List<ActionResult> results = agentService.executeGoal(longText);
        
        ActionResult result = results.get(0);
        assertTrue(result.isSuccess());
        assertEquals("SummarizeSkill", result.getSkillName());
        assertTrue(result.getOutput().length() <= 200, "Summary should be truncated to 200 chars");
    }

    @Test
    @DisplayName("Scenario 3.3: Alternative Keywords (tldr)")
    public void testTldrKeyword() {
        List<ActionResult> results = agentService.executeGoal(
            "tldr: Spring Boot is a framework that makes it easy to create stand-alone applications."
        );
        
        ActionResult result = results.get(0);
        assertTrue(result.isSuccess());
        assertEquals("SummarizeSkill", result.getSkillName());
        assertTrue(result.getOutput().contains("Spring Boot"));
    }

    // ===============================
    // 5. Error Handling Tests
    // ===============================

    @Test
    @DisplayName("Scenario 5.2: Unrecognized Command")
    public void testUnrecognizedCommand() {
        List<ActionResult> results = agentService.executeGoal("foobar: something random");
        
        ActionResult result = results.get(0);
        assertFalse(result.isSuccess(), "Unrecognized command should fail");
        assertEquals("none", result.getSkillName());
        assertTrue(result.getOutput().contains("No skill found to handle goal"));
    }

    @Test
    @DisplayName("Scenario 5.3: Invalid Calculator Expression")
    public void testInvalidCalculatorExpression() {
        List<ActionResult> results = agentService.executeGoal("calculate: abc + def");
        
        ActionResult result = results.get(0);
        assertEquals("CalculatorSkill", result.getSkillName());
        // The calculator should handle this gracefully (may return error or 0)
        assertNotNull(result.getOutput());
    }

    // ===============================
    // 7. Multi-Step Conversation Tests
    // ===============================

    @Test
    @DisplayName("Scenario 7.1: Sequential Different Skills")
    public void testSequentialDifferentSkills() {
        // Step 1: Calculator
        List<ActionResult> results1 = agentService.executeGoal("calculate: 5 + 5");
        ActionResult result1 = results1.get(0);
        assertTrue(result1.isSuccess());
        assertEquals("CalculatorSkill", result1.getSkillName());
        assertEquals("10.0", result1.getOutput());

        // Step 2: Search
        List<ActionResult> results2 = agentService.executeGoal("search: Spring AI");
        ActionResult result2 = results2.get(0);
        assertTrue(result2.isSuccess());
        assertEquals("MockSearchSkill", result2.getSkillName());

        // Step 3: Summarize
        List<ActionResult> results3 = agentService.executeGoal("summarize: This is a test.");
        ActionResult result3 = results3.get(0);
        assertTrue(result3.isSuccess());
        assertEquals("SummarizeSkill", result3.getSkillName());
    }

    @Test
    @DisplayName("Scenario 7.2: Multiple Calculator Operations")
    public void testMultipleCalculatorOperations() {
        // Step 1
        List<ActionResult> results1 = agentService.executeGoal("calculate: 10 * 2");
        assertEquals("20.0", results1.get(0).getOutput());

        // Step 2
        List<ActionResult> results2 = agentService.executeGoal("calculate: 20 + 5");
        assertEquals("25.0", results2.get(0).getOutput());

        // Step 3
        List<ActionResult> results3 = agentService.executeGoal("calculate: 25 / 5");
        assertEquals("5.0", results3.get(0).getOutput());
    }

    // ===============================
    // 8. Edge Cases
    // ===============================

    @Test
    @DisplayName("Scenario 8.2: Special Characters")
    public void testSpecialCharacters() {
        List<ActionResult> results = agentService.executeGoal("search: C++ programming & design");
        
        ActionResult result = results.get(0);
        assertTrue(result.isSuccess());
        assertEquals("MockSearchSkill", result.getSkillName());
        assertTrue(result.getOutput().contains("C++ programming & design"));
    }

    @Test
    @DisplayName("Scenario 8.3: Unicode Characters")
    public void testUnicodeCharacters() {
        List<ActionResult> results = agentService.executeGoal("search: 中文 Japanese 日本語");
        
        ActionResult result = results.get(0);
        assertTrue(result.isSuccess());
        assertEquals("MockSearchSkill", result.getSkillName());
        assertTrue(result.getOutput().contains("中文 Japanese 日本語"));
    }

    // ===============================
    // 9. MCP/Osquery Tests (Optional - requires MCP server)
    // ===============================

    @Test
    @DisplayName("Scenario 4.2: Get Hostname (Natural Language)")
    public void testOsqueryHostnameNaturalLanguage() {
        List<ActionResult> results = agentService.executeGoal("what is the hostname");
        
        ActionResult result = results.get(0);
        // If MCP is enabled, it should return OsqueryMCPSkill, otherwise error
        assertNotNull(result);
        if (result.isSuccess() && "OsqueryMCPSkill".equals(result.getSkillName())) {
            assertTrue(result.getOutput().contains("hostname"));
        }
        // If MCP is disabled, we just verify it doesn't crash
    }

    @Test
    @DisplayName("Scenario 4.3: Get Hostname (Simple Keyword)")
    public void testOsqueryHostnameKeyword() {
        List<ActionResult> results = agentService.executeGoal("hostname");
        
        ActionResult result = results.get(0);
        assertNotNull(result);
        if (result.isSuccess() && "OsqueryMCPSkill".equals(result.getSkillName())) {
            assertTrue(result.getOutput().contains("hostname"));
        }
    }

    @Test
    @DisplayName("Scenario 4.1: System Info Query")
    public void testOsquerySystemInfo() {
        List<ActionResult> results = agentService.executeGoal("show system info");
        
        ActionResult result = results.get(0);
        assertNotNull(result);
        if (result.isSuccess() && "OsqueryMCPSkill".equals(result.getSkillName())) {
            String output = result.getOutput();
            // Should contain system information fields
            assertTrue(output.contains("hostname") || output.contains("computer_name") || 
                      output.contains("cpu_brand"), "System info should contain relevant fields");
        }
    }

    @Test
    @DisplayName("Scenario 4.4: List Users")
    public void testOsqueryListUsers() {
        List<ActionResult> results = agentService.executeGoal("list users");
        
        ActionResult result = results.get(0);
        assertNotNull(result);
        if (result.isSuccess() && "OsqueryMCPSkill".equals(result.getSkillName())) {
            String output = result.getOutput();
            // Should contain user information
            assertTrue(output.contains("username") || output.contains("uid") || 
                      output.contains("shell"), "User list should contain user fields");
        }
    }

    @Test
    @DisplayName("Scenario 4.7: What is the System")
    public void testOsqueryWhatIsSystem() {
        List<ActionResult> results = agentService.executeGoal("what is the system");
        
        ActionResult result = results.get(0);
        assertNotNull(result);
        if (result.isSuccess() && "OsqueryMCPSkill".equals(result.getSkillName())) {
            assertNotNull(result.getOutput());
        }
    }

    // ===============================
    // Additional Validation Tests
    // ===============================

    @Test
    @DisplayName("Empty goal should return error")
    public void testEmptyGoal() {
        List<ActionResult> results = agentService.executeGoal("");
        
        ActionResult result = results.get(0);
        assertFalse(result.isSuccess());
        assertTrue(result.getOutput().contains("No skill found"));
    }

    @Test
    @DisplayName("Null goal should return error")
    public void testNullGoal() {
        List<ActionResult> results = agentService.executeGoal(null);
        
        ActionResult result = results.get(0);
        assertFalse(result.isSuccess());
        assertTrue(result.getOutput().contains("No skill found"));
    }

    @Test
    @DisplayName("Case insensitive command recognition")
    public void testCaseInsensitiveCommands() {
        // Test uppercase
        List<ActionResult> results1 = agentService.executeGoal("CALCULATE: 5 + 5");
        assertTrue(results1.get(0).isSuccess());
        assertEquals("CalculatorSkill", results1.get(0).getSkillName());

        // Test mixed case
        List<ActionResult> results2 = agentService.executeGoal("SeArCh: test query");
        assertTrue(results2.get(0).isSuccess());
        assertEquals("MockSearchSkill", results2.get(0).getSkillName());
    }
}
