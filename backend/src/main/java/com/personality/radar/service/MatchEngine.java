package com.personality.radar.service;

import com.personality.radar.domain.PersonalityDimension;
import java.util.Map;

public final class MatchEngine {
    private MatchEngine() {
    }

    public static double compatibilityScore(Map<String, Integer> left, Map<String, Integer> right) {
        double totalDiff = 0;
        for (PersonalityDimension dimension : PersonalityDimension.values()) {
            totalDiff += Math.abs(left.getOrDefault(dimension.name(), 50) - right.getOrDefault(dimension.name(), 50));
        }
        double score = 100.0 - totalDiff / PersonalityDimension.values().length;
        return Math.round(Math.max(0, score) * 10.0) / 10.0;
    }
}

