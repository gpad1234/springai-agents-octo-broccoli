package com.example.agentdemo;

import com.example.agentdemo.agent.AgentService;
import com.example.agentdemo.model.ActionResult;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AgentServiceTest {

    private static final Logger log = LoggerFactory.getLogger(AgentServiceTest.class);

    @Autowired
    AgentService agentService;

    @Test
    void testSummarizeSkill() {
        log.info("=== Testing SummarizeSkill ===");
        String input = "summarize: The quick brown fox jumps over the lazy dog. It is a classic pangram.";
        log.info("Input: {}", input);
        
        List<ActionResult> trace = agentService.executeGoal(input);
        assertFalse(trace.isEmpty());
        ActionResult r = trace.get(0);
        
        log.info("Skill: {}", r.getSkillName());
        log.info("Success: {}", r.isSuccess());
        log.info("Output: {}", r.getOutput());
        log.info("===========================\n");
        
        assertTrue(r.isSuccess());
        assertEquals("SummarizeSkill", r.getSkillName());
        assertTrue(r.getOutput().length() > 0);
    }

    @Test
    void testCalculatorSkill() {
        log.info("=== Testing CalculatorSkill ===");
        String input = "calculate: 12 / 4";
        log.info("Input: {}", input);
        
        List<ActionResult> trace = agentService.executeGoal(input);
        assertFalse(trace.isEmpty());
        ActionResult r = trace.get(0);
        
        log.info("Skill: {}", r.getSkillName());
        log.info("Success: {}", r.isSuccess());
        log.info("Output: {}", r.getOutput());
        log.info("===========================\n");
        
        assertTrue(r.isSuccess());
        assertEquals("CalculatorSkill", r.getSkillName());
        assertEquals("3.0", r.getOutput());
    }

    @Test
    void testMockSearchSkill() {
        log.info("=== Testing MockSearchSkill ===");
        String input = "search: example query";
        log.info("Input: {}", input);
        
        List<ActionResult> trace = agentService.executeGoal(input);
        assertFalse(trace.isEmpty());
        ActionResult r = trace.get(0);
        
        log.info("Skill: {}", r.getSkillName());
        log.info("Success: {}", r.isSuccess());
        log.info("Output: {}", r.getOutput());
        log.info("===========================\n");
        
        assertTrue(r.isSuccess());
        assertEquals("MockSearchSkill", r.getSkillName());
        assertTrue(r.getOutput().contains("Mock search results"));
    }
}
