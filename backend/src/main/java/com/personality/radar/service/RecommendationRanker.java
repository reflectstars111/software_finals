package com.personality.radar.service;

import java.util.List;
import java.util.Map;

/**
 * Computes the card percentage as an explainable fit score.
 *
 * Main signal: user profile versus recommendation tags.
 * Secondary signal: item base quality.
 * Small bounded nudges: user feedback history and admin rules.
 */
public final class RecommendationRanker {
    private static final double PROFILE_WEIGHT = 0.68;
    private static final double BASE_WEIGHT = 0.22;
    private static final double MAX_PREFERENCE_DELTA = 10.0;
    private static final double RULE_WEIGHT_TO_POINTS = 1.2;
    private static final double MAX_RULE_DELTA = 6.0;

    private RecommendationRanker() {
    }

    public static int score(
            int baseScore,
            List<String> tags,
            Map<String, Integer> preferences,
            Map<String, Integer> scores) {
        return score(baseScore, tags, preferences, scores, Map.of());
    }

    public static int score(
            int baseScore,
            List<String> tags,
            Map<String, Integer> preferences,
            Map<String, Integer> scores,
            Map<String, Integer> ruleWeights) {
        List<String> safeTags = tags == null ? List.of() : tags;
        Map<String, Integer> safePreferences = preferences == null ? Map.of() : preferences;
        Map<String, Integer> safeScores = scores == null ? Map.of() : scores;
        Map<String, Integer> safeRules = ruleWeights == null ? Map.of() : ruleWeights;

        double profileFit = averageTagValue(safeTags, tag -> tagProfileFit(tag, safeScores), 50.0);
        double preferenceDelta = averageTagValue(
                safeTags,
                tag -> Math.tanh(safePreferences.getOrDefault(tag, 0) / 18.0) * MAX_PREFERENCE_DELTA,
                0.0);
        double ruleDelta = averageTagValue(
                safeTags,
                tag -> clamp(safeRules.getOrDefault(tag, 0) * RULE_WEIGHT_TO_POINTS, -MAX_RULE_DELTA, MAX_RULE_DELTA),
                0.0);

        double raw = profileFit * PROFILE_WEIGHT
                + clamp(baseScore, 0, 100) * BASE_WEIGHT
                + preferenceDelta
                + ruleDelta;
        return (int) Math.round(clamp(raw, 0, 100));
    }

    private static double tagProfileFit(String tag, Map<String, Integer> scores) {
        return switch (tag) {
            case "explore" -> weightedAverage(scores, Map.of(
                    "OPENNESS", 0.45,
                    "FOOD_ADVENTURE", 0.25,
                    "TRAVEL_ADVENTURE", 0.30));
            case "structured" -> weightedAverage(scores, Map.of(
                    "CONSCIENTIOUSNESS", 0.65,
                    "TRAVEL_PLANNING", 0.25,
                    "EMOTIONAL_STABILITY", 0.10));
            case "social" -> weightedAverage(scores, Map.of(
                    "EXTRAVERSION", 0.50,
                    "SOCIAL_ENERGY", 0.30,
                    "FOOD_SOCIAL", 0.15,
                    "AGREEABLENESS", 0.05));
            case "gentle" -> weightedAverage(scores, Map.of(
                    "NEUROTICISM", 0.45,
                    "AGREEABLENESS", 0.25,
                    "INTROVERSION", 0.20,
                    "TRAVEL_PLANNING", 0.10));
            default -> 50.0;
        };
    }

    private static double weightedAverage(Map<String, Integer> scores, Map<String, Double> weights) {
        double total = 0.0;
        double weightTotal = 0.0;
        for (Map.Entry<String, Double> entry : weights.entrySet()) {
            total += dimensionScore(entry.getKey(), scores) * entry.getValue();
            weightTotal += entry.getValue();
        }
        return weightTotal == 0.0 ? 50.0 : total / weightTotal;
    }

    private static double dimensionScore(String dimension, Map<String, Integer> scores) {
        double raw = switch (dimension) {
            case "EMOTIONAL_STABILITY" -> 100 - score(scores, "NEUROTICISM");
            case "INTROVERSION" -> 100 - score(scores, "EXTRAVERSION");
            default -> score(scores, dimension);
        };
        // Amplify deviation from neutral for wider spread
        return 50.0 + (raw - 50.0) * 1.6;
    }

    private static int score(Map<String, Integer> scores, String dimension) {
        return (int) Math.round(clamp(scores.getOrDefault(dimension, 50), 0, 100));
    }

    private static double averageTagValue(List<String> tags, TagScorer scorer, double fallback) {
        if (tags.isEmpty()) {
            return fallback;
        }
        double total = 0.0;
        for (String tag : tags) {
            total += scorer.value(tag);
        }
        return total / tags.size();
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    @FunctionalInterface
    private interface TagScorer {
        double value(String tag);
    }
}
