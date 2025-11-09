package com.example.agentdemo.agent.skills;

import com.example.agentdemo.agent.Skill;
import com.example.agentdemo.model.ActionResult;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CalculatorSkill implements Skill {
    private static final Pattern SIMPLE_EXPR = Pattern.compile("([-+]?[0-9]*\\.?[0-9]+)\\s*([+\\-*/])\\s*([-+]?[0-9]*\\.?[0-9]+)");

    @Override
    public boolean canHandle(String goal) {
        if (goal == null) return false;
        String g = goal.toLowerCase();
        // Match calculate/compute, or if it starts with "sum:" (but not "summarize:")
        return g.contains("calculate") || g.contains("compute") || 
               (g.contains("sum") && !g.contains("summarize") && !g.contains("summarise")) || 
               g.matches(".*\\d\\+.*");
    }

    @Override
    public ActionResult execute(String goal) {
        String expr = goal;
        // try to extract an expression like 'calculate: 2+3' or '2 + 3'
        if (goal.contains(":")) {
            expr = goal.substring(goal.indexOf(":") + 1).trim();
        }

        Matcher m = SIMPLE_EXPR.matcher(expr);
        if (m.find()) {
            try {
                double a = Double.parseDouble(m.group(1));
                String op = m.group(2);
                double b = Double.parseDouble(m.group(3));
                double res = 0;
                switch (op) {
                    case "+": res = a + b; break;
                    case "-": res = a - b; break;
                    case "*": res = a * b; break;
                    case "/": res = b == 0 ? Double.NaN : a / b; break;
                }
                return new ActionResult(true, "CalculatorSkill", String.valueOf(res));
            } catch (Exception e) {
                return new ActionResult(false, "CalculatorSkill", "Error evaluating expression: " + e.getMessage());
            }
        }

        return new ActionResult(false, "CalculatorSkill", "No simple expression found in: " + goal);
    }
}
