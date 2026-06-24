package com.personality.radar.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RecommendationRankerTest {
    @Test
    void highExploreFitGetsStrongPercentage() {
        int score = RecommendationRanker.score(
                65, List.of("explore"), Map.of(),
                Map.of("OPENNESS", 80, "FOOD_ADVENTURE", 80, "TRAVEL_ADVENTURE", 80), Map.of());
        assertThat(score).isEqualTo(81);
    }

    @Test
    void lowExploreFitIsClearlyLower() {
        int score = RecommendationRanker.score(
                65, List.of("explore"), Map.of(),
                Map.of("OPENNESS", 30, "FOOD_ADVENTURE", 30, "TRAVEL_ADVENTURE", 30), Map.of());
        assertThat(score).isEqualTo(27);
    }

    @Test
    void gentleTagMatchesHighEmotionalSensitivity() {
        int sensitiveUser = RecommendationRanker.score(
                65, List.of("gentle"), Map.of(),
                Map.of("AGREEABLENESS", 60, "NEUROTICISM", 80), Map.of());
        int steadyUser = RecommendationRanker.score(
                65, List.of("gentle"), Map.of(),
                Map.of("AGREEABLENESS", 60, "NEUROTICISM", 20), Map.of());
        assertThat(sensitiveUser).isEqualTo(66);
        assertThat(steadyUser).isEqualTo(36);
        assertThat(sensitiveUser).isGreaterThan(steadyUser);
    }

    @Test
    void feedbackIsBoundedNudge() {
        int neutral = RecommendationRanker.score(
                65, List.of("gentle"), Map.of(),
                Map.of("AGREEABLENESS", 60, "NEUROTICISM", 50), Map.of());
        int liked = RecommendationRanker.score(
                65, List.of("gentle"), Map.of("gentle", 24),
                Map.of("AGREEABLENESS", 60, "NEUROTICISM", 50), Map.of());
        assertThat(neutral).isEqualTo(51);
        assertThat(liked).isEqualTo(60);
    }

    @Test
    void ruleWeightAddsSmallNudge() {
        int withoutRule = RecommendationRanker.score(
                65, List.of("structured"), Map.of(),
                Map.of("CONSCIENTIOUSNESS", 70, "TRAVEL_PLANNING", 60), Map.of());
        int withRule = RecommendationRanker.score(
                65, List.of("structured"), Map.of(),
                Map.of("CONSCIENTIOUSNESS", 70, "TRAVEL_PLANNING", 60), Map.of("structured", 4));
        assertThat(withoutRule).isEqualTo(65);
        assertThat(withRule).isEqualTo(70);
    }

    @Test
    void multiTagItemAveragesAndKeepsNudges() {
        int score = RecommendationRanker.score(
                68, List.of("explore", "social"),
                Map.of("explore", 6, "social", 3),
                Map.of("OPENNESS", 80, "FOOD_ADVENTURE", 80, "TRAVEL_ADVENTURE", 80,
                        "EXTRAVERSION", 70, "SOCIAL_ENERGY", 70, "FOOD_SOCIAL", 70),
                Map.of("explore", 4, "social", 3));
        assertThat(score).isEqualTo(82);
    }
}
