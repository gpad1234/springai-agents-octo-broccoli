package com.example.agentdemo.controller;

import com.example.agentdemo.agent.AgentService;
import com.example.agentdemo.model.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agent")
public class AgentController {
    private static final Logger log = LoggerFactory.getLogger(AgentController.class);
    
    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping("/execute")
    public ResponseEntity<Map<String, Object>> execute(@RequestBody Map<String, String> body) {
        log.info("Controller called with body: {}", body);
        try {
            String goal = body.getOrDefault("goal", "");
            log.info("Executing goal: {}", goal);
            List<ActionResult> trace = agentService.executeGoal(goal);

            String finalOutput = trace.get(trace.size() - 1).getOutput();
            log.info("Final output: {}", finalOutput);

            Map<String, Object> response = new HashMap<>();
            response.put("goal", goal);
            response.put("trace", trace);
            response.put("finalOutput", finalOutput);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error executing goal", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage() != null ? e.getMessage() : "Unknown error");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
