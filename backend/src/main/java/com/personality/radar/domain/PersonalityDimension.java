package com.personality.radar.domain;

public enum PersonalityDimension {
    OPENNESS("开放性"),
    CONSCIENTIOUSNESS("尽责性"),
    EXTRAVERSION("外向性"),
    AGREEABLENESS("宜人性"),
    NEUROTICISM("情绪敏感度");

    private final String label;

    PersonalityDimension(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
