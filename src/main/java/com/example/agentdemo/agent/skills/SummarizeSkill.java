package com.example.agentdemo.agent.skills;

import com.example.agentdemo.agent.Skill;
import com.example.agentdemo.model.ActionResult;
import org.springframework.stereotype.Component;

@Component
public class SummarizeSkill implements Skill {
    @Override
    public boolean canHandle(String goal) {
        if (goal == null) return false;
        String g = goal.toLowerCase();
        return g.contains("summarize") || g.contains("summary") || g.contains("summarise") || g.startsWith("summarize:") || g.startsWith("summary:");
    }

    @Override
    public ActionResult execute(String goal) {
        String payload = goal;
        // allow format 'summarize: TEXT'
        if (goal.contains(":")) {
            payload = goal.substring(goal.indexOf(":") + 1).trim();
        }

        if (payload.isEmpty()) {
            return new ActionResult(false, "SummarizeSkill", "No text to summarize");
        }

        // naive "summary": return first sentence or first 120 chars
        int period = payload.indexOf('.');
        String summary;
        if (period > 0 && period < 200) {
            summary = payload.substring(0, period + 1).trim();
        } else if (payload.length() > 120) {
            summary = payload.substring(0, 120).trim() + "...";
        } else {
            summary = payload;
        }

        return new ActionResult(true, "SummarizeSkill", summary);
    }
}
