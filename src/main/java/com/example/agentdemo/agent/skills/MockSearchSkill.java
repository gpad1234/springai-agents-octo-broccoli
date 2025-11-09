package com.example.agentdemo.agent.skills;

import com.example.agentdemo.agent.Skill;
import com.example.agentdemo.model.ActionResult;
import org.springframework.stereotype.Component;

@Component
public class MockSearchSkill implements Skill {
    @Override
    public boolean canHandle(String goal) {
        if (goal == null) return false;
        String g = goal.toLowerCase();
        return g.contains("search") || g.contains("find") || g.contains("lookup");
    }

    @Override
    public ActionResult execute(String goal) {
        // Return a mocked search result for demo purposes
        String query = goal;
        if (goal.contains(":")) {
            query = goal.substring(goal.indexOf(":") + 1).trim();
        }

        String output = "Mock search results for '" + query + "':\n" +
                "1) Example result A - short description\n" +
                "2) Example result B - short description\n" +
                "3) Example result C - short description";

        return new ActionResult(true, "MockSearchSkill", output);
    }
}
