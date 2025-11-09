package com.example.agentdemo.agent;

import com.example.agentdemo.model.ActionResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AgentService {
    private final List<Skill> skills;

    public AgentService(List<Skill> skills) {
        this.skills = skills;
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
