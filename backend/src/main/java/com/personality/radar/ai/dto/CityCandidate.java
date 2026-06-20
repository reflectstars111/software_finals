package com.personality.radar.ai.dto;

public class CityCandidate {

    private String name;

    private String reason; // AI解释（可选）

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}