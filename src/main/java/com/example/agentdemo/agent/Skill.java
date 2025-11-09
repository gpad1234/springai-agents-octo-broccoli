package com.example.agentdemo.agent;

import com.example.agentdemo.model.ActionResult;

public interface Skill {
    boolean canHandle(String goal);
    ActionResult execute(String goal);
}
