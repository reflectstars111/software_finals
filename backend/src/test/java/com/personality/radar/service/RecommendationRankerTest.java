package com.personality.radar.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RecommendationRankerTest {
    @Test
    void appliesTagsPersonalitySignalsAndFeedbackPreferences() {
        int score = RecommendationRanker.score(
                60,
                List.of("explore", "social"),
                Map.of("explore", 8),
                Map.of("OPENNESS", 80, "EXTRAVERSION", 70));

        assertThat(score).isEqualTo(78);
    }

    @Test
    void clampsScoresToZeroAndOneHundred() {
        assertThat(RecommendationRanker.score(98, List.of("explore"), Map.of("explore", 30), Map.of("OPENNESS", 100)))
                .isEqualTo(100);
        assertThat(RecommendationRanker.score(3, List.of("structured"), Map.of("structured", -30), Map.of("CONSCIENTIOUSNESS", 0)))
                .isEqualTo(0);
    }
}
