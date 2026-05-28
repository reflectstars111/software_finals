package com.personality.radar.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class MatchEngineTest {
    @Test
    void returnsOneHundredForIdenticalProfiles() {
        Map<String, Integer> profile = Map.of(
                "OPENNESS", 70,
                "CONSCIENTIOUSNESS", 60,
                "EXTRAVERSION", 50,
                "AGREEABLENESS", 80,
                "NEUROTICISM", 40);

        assertThat(MatchEngine.compatibilityScore(profile, profile)).isEqualTo(100.0);
    }

    @Test
    void subtractsAverageDimensionDifferenceAndKeepsOneDecimal() {
        double score = MatchEngine.compatibilityScore(
                Map.of("OPENNESS", 80, "CONSCIENTIOUSNESS", 70, "EXTRAVERSION", 60, "AGREEABLENESS", 50, "NEUROTICISM", 40),
                Map.of("OPENNESS", 50, "CONSCIENTIOUSNESS", 50, "EXTRAVERSION", 50, "AGREEABLENESS", 50, "NEUROTICISM", 50));

        assertThat(score).isEqualTo(86.0);
    }
}
