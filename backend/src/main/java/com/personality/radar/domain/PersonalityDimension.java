package com.personality.radar.domain;

public enum PersonalityDimension {
    OPENNESS("开放性"),
    CONSCIENTIOUSNESS("尽责性"),
    EXTRAVERSION("外向性"),
    AGREEABLENESS("宜人性"),
    NEUROTICISM("情绪敏感度"),
    FOOD_ADVENTURE("饮食探索"),
    FOOD_SOCIAL("饮食社交"),
    TRAVEL_ADVENTURE("旅行探索"),
    TRAVEL_PLANNING("旅行计划"),
    SOCIAL_ENERGY("社交能量");

    private final String label;

    PersonalityDimension(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
