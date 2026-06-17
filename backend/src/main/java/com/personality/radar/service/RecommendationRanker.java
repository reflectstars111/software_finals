package com.personality.radar.service;

import java.util.List;
import java.util.Map;

public final class RecommendationRanker {
    private RecommendationRanker() {
    }

    public static int score(int baseScore, List<String> tags, Map<String, Integer> preferences, Map<String, Integer> scores) {
        return score(baseScore, tags, preferences, scores, Map.of());
    }

    public static int score(
            int baseScore,
            List<String> tags,
            Map<String, Integer> preferences,
            Map<String, Integer> scores,
            Map<String, Integer> ruleWeights) {
        int result = baseScore;
        for (String tag : tags) {
            result += preferences.getOrDefault(tag, 0);
            result += ruleWeights.getOrDefault(tag, 0);
        }
        int openness = scores.getOrDefault("OPENNESS", 50);
        int conscientiousness = scores.getOrDefault("CONSCIENTIOUSNESS", 50);
        int extraversion = scores.getOrDefault("EXTRAVERSION", 50);
        int agreeableness = scores.getOrDefault("AGREEABLENESS", 50);
        int foodAdventure = scores.getOrDefault("FOOD_ADVENTURE", 50);
        int foodSocial = scores.getOrDefault("FOOD_SOCIAL", 50);
        int travelAdventure = scores.getOrDefault("TRAVEL_ADVENTURE", 50);
        int travelPlanning = scores.getOrDefault("TRAVEL_PLANNING", 50);
        int socialEnergy = scores.getOrDefault("SOCIAL_ENERGY", 50);
        if (tags.contains("explore")) {
            result += (openness - 50) / 5;
            result += (foodAdventure - 50) / 6;
            result += (travelAdventure - 50) / 6;
        }
        if (tags.contains("structured")) {
            result += (conscientiousness - 50) / 5;
            result += (travelPlanning - 50) / 6;
        }
        if (tags.contains("social")) {
            result += (extraversion - 50) / 5;
            result += (foodSocial - 50) / 6;
            result += (socialEnergy - 50) / 6;
        }
        if (tags.contains("gentle")) {
            result += (agreeableness - 50) / 5;
            int emotionalStability = 100 - scores.getOrDefault("NEUROTICISM", 50);
            result += (emotionalStability - 50) / 6;
        }
        return Math.max(0, Math.min(100, result));
    }
}
