package com.personality.radar.service;

import com.personality.radar.common.BusinessException;
import com.personality.radar.domain.Feedback;
import com.personality.radar.domain.FeedbackRating;
import com.personality.radar.domain.RecommendationItem;
import com.personality.radar.domain.RecommendationRule;
import com.personality.radar.domain.SceneType;
import com.personality.radar.domain.TestResult;
import com.personality.radar.domain.TestType;
import com.personality.radar.domain.UserAccount;
import com.personality.radar.domain.UserPreference;
import com.personality.radar.dto.ApiDtos;
import com.personality.radar.repository.FeedbackRepository;
import com.personality.radar.repository.RecommendationItemRepository;
import com.personality.radar.repository.RecommendationRuleRepository;
import com.personality.radar.repository.TestResultRepository;
import com.personality.radar.repository.UserPreferenceRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecommendationService {
    private final RecommendationItemRepository items;
    private final TestResultRepository results;
    private final FeedbackRepository feedbacks;
    private final UserPreferenceRepository preferences;
    private final RecommendationRuleRepository rules;

    public RecommendationService(
            RecommendationItemRepository items,
            TestResultRepository results,
            FeedbackRepository feedbacks,
            UserPreferenceRepository preferences,
            RecommendationRuleRepository rules) {
        this.items = items;
        this.results = results;
        this.feedbacks = feedbacks;
        this.preferences = preferences;
        this.rules = rules;
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.RecommendationResponse> recommend(UserAccount user, String sceneValue) {
        SceneType scene = EnumParser.sceneType(sceneValue);
        TestResult result = results.findFirstByUserAndTypeOrderByCreatedAtDesc(user, TestType.PERSONALITY)
                .orElseThrow(() -> new BusinessException(400, "请先完成基础性格测试"));
        Map<String, Integer> preferenceMap = preferences.findByUser(user).stream()
                .collect(Collectors.toMap(UserPreference::getTag, UserPreference::getWeight));
        Map<String, Integer> ruleMap = rules.findByActiveTrueOrderByTagAsc().stream()
                .collect(Collectors.toMap(RecommendationRule::getTag, RecommendationRule::getWeight));
        return items.findBySceneAndActiveTrue(scene).stream()
                .map(item -> DtoMapper.recommendation(item,
                        RecommendationRanker.score(item.getBaseScore(), item.getTags(), preferenceMap, result.getScores(), ruleMap)))
                .sorted(Comparator.comparing(ApiDtos.RecommendationResponse::score).reversed())
                .toList();
    }

    @Transactional
    public void feedback(UserAccount user, Long itemId, ApiDtos.FeedbackRequest request) {
        RecommendationItem item = items.findById(itemId)
                .orElseThrow(() -> new BusinessException(404, "推荐项不存在"));
        FeedbackRating rating = EnumParser.rating(request.rating());
        Feedback feedback = new Feedback();
        feedback.setUser(user);
        feedback.setItem(item);
        feedback.setRating(rating);
        feedback.setComment(request.comment());
        feedbacks.save(feedback);

        int delta = switch (rating) {
            case LIKE -> 8;
            case NEUTRAL -> 2;
            case DISLIKE -> -8;
        };
        for (String tag : item.getTags()) {
            UserPreference preference = preferences.findByUserAndTag(user, tag).orElseGet(() -> {
                UserPreference created = new UserPreference();
                created.setUser(user);
                created.setTag(tag);
                return created;
            });
            preference.setWeight(Math.max(-30, Math.min(30, preference.getWeight() + delta)));
            preferences.save(preference);
        }
    }
}
