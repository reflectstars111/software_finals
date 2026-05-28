package com.personality.radar.service;

import com.personality.radar.domain.PersonalityDimension;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ScoringEngine {
    private ScoringEngine() {
    }

    public static Map<String, Integer> normalizeScores(Map<String, List<Integer>> rawWeights) {
        Map<String, Integer> scores = new LinkedHashMap<>();
        for (PersonalityDimension dimension : PersonalityDimension.values()) {
            List<Integer> values = rawWeights.getOrDefault(dimension.name(), new ArrayList<>());
            double average = values.isEmpty() ? 3.0 : values.stream().mapToInt(Integer::intValue).average().orElse(3.0);
            int score = (int) Math.round(Math.max(0, Math.min(100, (average - 1.0) / 4.0 * 100.0)));
            scores.put(dimension.name(), score);
        }
        return scores;
    }
}

