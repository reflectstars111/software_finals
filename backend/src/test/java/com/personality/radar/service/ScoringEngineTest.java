package com.personality.radar.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ScoringEngineTest {
    @Test
    void normalizesAverageWeightsToZeroToOneHundredScores() {
        Map<String, Integer> scores = ScoringEngine.normalizeScores(Map.of(
                "OPENNESS", List.of(1, 5),
                "CONSCIENTIOUSNESS", List.of(5, 5),
                "EXTRAVERSION", List.of(1, 1),
                "AGREEABLENESS", List.of(4),
                "NEUROTICISM", List.of(3)));

        assertThat(scores).containsEntry("OPENNESS", 50);
        assertThat(scores).containsEntry("CONSCIENTIOUSNESS", 100);
        assertThat(scores).containsEntry("EXTRAVERSION", 0);
        assertThat(scores).containsEntry("AGREEABLENESS", 75);
        assertThat(scores).containsEntry("NEUROTICISM", 50);
    }

    @Test
    void defaultsMissingDimensionsToNeutralScore() {
        Map<String, Integer> scores = ScoringEngine.normalizeScores(Map.of());

        assertThat(scores.values()).containsOnly(50);
    }
}
