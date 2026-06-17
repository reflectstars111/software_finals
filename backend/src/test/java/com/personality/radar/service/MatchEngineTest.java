package com.personality.radar.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class MatchEngineTest {
    @Test
    void returnsOneHundredForIdenticalProfiles() {
        Map<String, Integer> profile = Map.of(
                "OPENNESS", 70, "CONSCIENTIOUSNESS", 60, "EXTRAVERSION", 50,
                "AGREEABLENESS", 80, "NEUROTICISM", 40,
                "FOOD_ADVENTURE", 65, "FOOD_SOCIAL", 55, "TRAVEL_ADVENTURE", 70,
                "TRAVEL_PLANNING", 60, "SOCIAL_ENERGY", 75);
        assertThat(MatchEngine.compatibilityScore(profile, profile)).isEqualTo(100.0);
    }

    @Test
    void subtractsAverageDimensionDifferenceAcrossAllTenDimensions() {
        double score = MatchEngine.compatibilityScore(
                Map.of("OPENNESS", 80, "CONSCIENTIOUSNESS", 70, "EXTRAVERSION", 60,
                        "AGREEABLENESS", 50, "NEUROTICISM", 40,
                        "FOOD_ADVENTURE", 80, "FOOD_SOCIAL", 70, "TRAVEL_ADVENTURE", 60,
                        "TRAVEL_PLANNING", 50, "SOCIAL_ENERGY", 40),
                Map.of("OPENNESS", 50, "CONSCIENTIOUSNESS", 50, "EXTRAVERSION", 50,
                        "AGREEABLENESS", 50, "NEUROTICISM", 50,
                        "FOOD_ADVENTURE", 50, "FOOD_SOCIAL", 50, "TRAVEL_ADVENTURE", 50,
                        "TRAVEL_PLANNING", 50, "SOCIAL_ENERGY", 50));
        assertThat(score).isEqualTo(86.0);
    }

    @Test
    void handlesZeroDifferenceOnAllDimensions() {
        Map<String, Integer> all50 = Map.of(
                "OPENNESS", 50, "CONSCIENTIOUSNESS", 50, "EXTRAVERSION", 50,
                "AGREEABLENESS", 50, "NEUROTICISM", 50,
                "FOOD_ADVENTURE", 50, "FOOD_SOCIAL", 50, "TRAVEL_ADVENTURE", 50,
                "TRAVEL_PLANNING", 50, "SOCIAL_ENERGY", 50);
        assertThat(MatchEngine.compatibilityScore(all50, all50)).isEqualTo(100.0);
    }

    @Test
    void handlesMaximumDifference() {
        double score = MatchEngine.compatibilityScore(
                Map.of("OPENNESS", 100, "CONSCIENTIOUSNESS", 100, "EXTRAVERSION", 100,
                        "AGREEABLENESS", 100, "NEUROTICISM", 100,
                        "FOOD_ADVENTURE", 100, "FOOD_SOCIAL", 100, "TRAVEL_ADVENTURE", 100,
                        "TRAVEL_PLANNING", 100, "SOCIAL_ENERGY", 100),
                Map.of("OPENNESS", 0, "CONSCIENTIOUSNESS", 0, "EXTRAVERSION", 0,
                        "AGREEABLENESS", 0, "NEUROTICISM", 0,
                        "FOOD_ADVENTURE", 0, "FOOD_SOCIAL", 0, "TRAVEL_ADVENTURE", 0,
                        "TRAVEL_PLANNING", 0, "SOCIAL_ENERGY", 0));
        assertThat(score).isEqualTo(0.0);
    }
}
