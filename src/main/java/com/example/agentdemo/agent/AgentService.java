package com.example.agentdemo.agent;

import com.example.agentdemo.model.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AgentService {
    private static final Logger logger = LoggerFactory.getLogger(AgentService.class);
    private final List<Skill> skills;

    public AgentService(List<Skill> skills) {
        this.skills = skills;
    }

    @PostConstruct
    public void init() {
        logger.info("=".repeat(60));
        logger.info("🤖 Agent Service Initialized with {} skill(s)", skills.size());
        logger.info("=".repeat(60));
        for (int i = 0; i < skills.size(); i++) {
            Skill skill = skills.get(i);
            String skillName = skill.getClass().getSimpleName();
            logger.info("  {}. {} - Ready", i + 1, skillName);
        }
        logger.info("=".repeat(60));
    }

    /**
     * Get list of all available skill names
     */
    public List<String> getAvailableSkills() {
        return skills.stream()
                .map(skill -> skill.getClass().getSimpleName())
                .collect(Collectors.toList());
    }

    /**
     * Execute the given goal by picking the first skill that declares it can handle the goal.
     * Returns an execution trace (list of ActionResult) and the final output (from the chosen skill).
     */
    public List<ActionResult> executeGoal(String goal) {
        List<ActionResult> trace = new ArrayList<>();

        for (Skill skill : skills) {
            if (skill.canHandle(goal)) {
                ActionResult result = skill.execute(goal);
                trace.add(result);
                return trace;
            }
        }

        // fallback: no skill could handle the goal
        ActionResult fallback = new ActionResult(false, "none", "No skill found to handle goal: " + goal);
        trace.add(fallback);
        return trace;
    }
}
