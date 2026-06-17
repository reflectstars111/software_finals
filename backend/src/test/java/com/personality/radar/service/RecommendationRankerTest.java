package com.personality.radar.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RecommendationRankerTest {
    @Test
    void appliesExploreTagsWithOpennessAndLifestyleSignals() {
        int score = RecommendationRanker.score(
                60, List.of("explore"),
                Map.of("explore", 8),
                Map.of("OPENNESS", 80, "FOOD_ADVENTURE", 80, "TRAVEL_ADVENTURE", 80));
        assertThat(score).isEqualTo(84);
    }

    @Test
    void appliesSocialTagsWithExtraversionAndLifestyleSignals() {
        int score = RecommendationRanker.score(
                60, List.of("social"),
                Map.of("social", 0),
                Map.of("EXTRAVERSION", 70, "FOOD_SOCIAL", 70, "SOCIAL_ENERGY", 70));
        assertThat(score).isEqualTo(70);
    }

    @Test
    void appliesStructuredTagWithConscientiousnessAndTravelPlanning() {
        int score = RecommendationRanker.score(
                60, List.of("structured"),
                Map.of("structured", 0),
                Map.of("CONSCIENTIOUSNESS", 80, "TRAVEL_PLANNING", 80));
        assertThat(score).isEqualTo(71);
    }

    @Test
    void appliesGentleTagWithAgreeablenessAndEmotionalStability() {
        int score = RecommendationRanker.score(
                60, List.of("gentle"),
                Map.of("gentle", 0),
                Map.of("AGREEABLENESS", 80, "NEUROTICISM", 20));
        assertThat(score).isEqualTo(71);
    }

    @Test
    void clampsScoresToZeroAndOneHundred() {
        assertThat(RecommendationRanker.score(98, List.of("explore"), Map.of("explore", 30), Map.of("OPENNESS", 100)))
                .isEqualTo(100);
        assertThat(RecommendationRanker.score(3, List.of("structured"), Map.of("structured", -30), Map.of("CONSCIENTIOUSNESS", 0)))
                .isEqualTo(0);
    }

    @Test
    void includesRuleWeightsInScoring() {
        int score = RecommendationRanker.score(
                60, List.of("explore"), Map.of("explore", 8),
                Map.of("OPENNESS", 80), Map.of("explore", 4));
        assertThat(score).isEqualTo(78);
    }
}
