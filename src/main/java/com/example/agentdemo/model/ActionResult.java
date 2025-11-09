package com.example.agentdemo.model;

public class ActionResult {
    private boolean success;
    private String skillName;
    private String output;

    public ActionResult() {}

    public ActionResult(boolean success, String skillName, String output) {
        this.success = success;
        this.skillName = skillName;
        this.output = output;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }
}
