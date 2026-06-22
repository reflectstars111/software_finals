package com.personality.radar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.personality.radar.domain.Feedback;
import com.personality.radar.domain.RecommendationItem;
import com.personality.radar.domain.SceneType;
import com.personality.radar.domain.TestResult;
import com.personality.radar.domain.TestType;
import com.personality.radar.domain.UserAccount;
import com.personality.radar.dto.ApiDtos;
import com.personality.radar.repository.FeedbackRepository;
import com.personality.radar.repository.RecommendationItemRepository;
import com.personality.radar.repository.RecommendationRuleRepository;
import com.personality.radar.repository.TestResultRepository;
import com.personality.radar.repository.UserPreferenceRepository;
import com.personality.radar.service.AiRecommendationService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RecommendationServiceTest {
    @Test
    void recommendationScoreUsesLatestLifestyleScoresAcrossTestTypes() {
        RecommendationItemRepository items = mock(RecommendationItemRepository.class);
        TestResultRepository results = mock(TestResultRepository.class);
        FeedbackRepository feedbacks = mock(FeedbackRepository.class);
        UserPreferenceRepository preferences = mock(UserPreferenceRepository.class);
        RecommendationRuleRepository rules = mock(RecommendationRuleRepository.class);
        AiRecommendationService aiRec = mock(AiRecommendationService.class);
        RecommendationService service = new RecommendationService(items, results, feedbacks, preferences, rules, aiRec);

        UserAccount user = new UserAccount();
        user.setPhone("13800000000");
        user.setDisplayName("Tester");

        TestResult personality = result(user, TestType.PERSONALITY, Map.of(
                "OPENNESS", 70,
                "CONSCIENTIOUSNESS", 50,
                "EXTRAVERSION", 50,
                "AGREEABLENESS", 50,
                "NEUROTICISM", 50));
        TestResult food = result(user, TestType.FOOD, Map.of("FOOD_ADVENTURE", 95));
        RecommendationItem item = item(SceneType.FOOD, 65, "explore");

        when(results.findFirstByUserAndTypeOrderByCreatedAtDesc(user, TestType.PERSONALITY))
                .thenReturn(Optional.of(personality));
        when(results.findFirstByUserAndTypeOrderByCreatedAtDesc(user, TestType.FOOD))
                .thenReturn(Optional.of(food));
        when(results.findFirstByUserAndTypeOrderByCreatedAtDesc(user, TestType.TRAVEL))
                .thenReturn(Optional.empty());
        when(results.findFirstByUserAndTypeOrderByCreatedAtDesc(user, TestType.SOCIAL))
                .thenReturn(Optional.empty());
        when(items.findBySceneAndActiveTrue(SceneType.FOOD)).thenReturn(List.of(item));
        when(preferences.findByUser(user)).thenReturn(List.of());
        when(rules.findByActiveTrueOrderByTagAsc()).thenReturn(List.of());
        when(feedbacks.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.<Feedback>of());

        List<ApiDtos.RecommendationResponse> recommendations = service.recommend(user, "food");

        assertThat(recommendations).hasSize(1);
        assertThat(recommendations.get(0).score()).isEqualTo(70);
    }

    private static TestResult result(UserAccount user, TestType type, Map<String, Integer> scores) {
        TestResult result = new TestResult();
        result.setUser(user);
        result.setType(type);
        result.setScores(scores);
        return result;
    }

    private static RecommendationItem item(SceneType scene, int baseScore, String... tags) {
        RecommendationItem item = new RecommendationItem();
        item.setScene(scene);
        item.setTitle("sample");
        item.setDescription("sample");
        item.setBaseScore(baseScore);
        item.setTags(List.of(tags));
        item.setActive(true);
        return item;
    }
}
