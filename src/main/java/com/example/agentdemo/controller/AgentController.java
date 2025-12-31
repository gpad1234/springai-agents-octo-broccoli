package com.example.agentdemo.controller;

import com.example.agentdemo.agent.AgentService;
import com.example.agentdemo.model.ActionResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agent")
public class AgentController {
    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @GetMapping("/skills")
    public ResponseEntity<Map<String, Object>> getSkills() {
        List<String> skills = agentService.getAvailableSkills();
        return ResponseEntity.ok(Map.of(
                "count", skills.size(),
                "skills", skills
        ));
    }

    @PostMapping("/execute")
    public ResponseEntity<Map<String, Object>> execute(@RequestBody Map<String, String> body) {
        String goal = body.getOrDefault("goal", "");
        List<ActionResult> trace = agentService.executeGoal(goal);

        String finalOutput = trace.get(trace.size() - 1).getOutput();

        return ResponseEntity.ok(Map.of(
                "goal", goal,
                "trace", trace,
                "finalOutput", finalOutput
        ));
    }

    @GetMapping("/message")
    public ResponseEntity<Map<String, String>> getMessage() {
        return ResponseEntity.ok(Map.of("message", "Hello from Spring AI Agent Server!"));
    }
}
