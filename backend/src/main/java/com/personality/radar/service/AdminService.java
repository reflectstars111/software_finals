package com.personality.radar.service;

import com.personality.radar.common.BusinessException;
import com.personality.radar.domain.AdminLog;
import com.personality.radar.domain.FeedbackRating;
import com.personality.radar.domain.Question;
import com.personality.radar.domain.QuestionOption;
import com.personality.radar.domain.RecommendationItem;
import com.personality.radar.domain.RecommendationRule;
import com.personality.radar.domain.Role;
import com.personality.radar.domain.SceneType;
import com.personality.radar.domain.TestType;
import com.personality.radar.domain.UserAccount;
import com.personality.radar.dto.ApiDtos;
import com.personality.radar.repository.AdminLogRepository;
import com.personality.radar.repository.CompatibilityReportRepository;
import com.personality.radar.repository.FeedbackRepository;
import com.personality.radar.repository.QuestionRepository;
import com.personality.radar.repository.RecommendationItemRepository;
import com.personality.radar.repository.RecommendationRuleRepository;
import com.personality.radar.repository.ShareLinkRepository;
import com.personality.radar.repository.TestResultRepository;
import com.personality.radar.repository.UserRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {
    private final QuestionRepository questions;
    private final RecommendationItemRepository items;
    private final FeedbackRepository feedbacks;
    private final UserRepository users;
    private final CompatibilityReportRepository matches;
    private final AdminLogRepository logs;
    private final TestResultRepository testResults;
    private final ShareLinkRepository shares;
    private final RecommendationRuleRepository rules;

    public AdminService(
            QuestionRepository questions,
            RecommendationItemRepository items,
            FeedbackRepository feedbacks,
            UserRepository users,
            CompatibilityReportRepository matches,
            AdminLogRepository logs,
            TestResultRepository testResults,
            ShareLinkRepository shares,
            RecommendationRuleRepository rules) {
        this.questions = questions;
        this.items = items;
        this.feedbacks = feedbacks;
        this.users = users;
        this.matches = matches;
        this.logs = logs;
        this.testResults = testResults;
        this.shares = shares;
        this.rules = rules;
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.QuestionResponse> allQuestions() {
        return questions.findAll().stream().map(DtoMapper::question).toList();
    }

    @Transactional
    public ApiDtos.QuestionResponse createQuestion(UserAccount admin, ApiDtos.AdminQuestionRequest request) {
        Question question = toQuestion(new Question(), request);
        questions.save(question);
        log(admin, "CREATE_QUESTION", question.getContent());
        return DtoMapper.question(question);
    }

    @Transactional
    public ApiDtos.QuestionResponse updateQuestion(UserAccount admin, Long id, ApiDtos.AdminQuestionRequest request) {
        Question question = questions.findById(id).orElseThrow(() -> new BusinessException(404, "题目不存在"));
        question.getOptions().clear();
        toQuestion(question, request);
        log(admin, "UPDATE_QUESTION", question.getContent());
        return DtoMapper.question(question);
    }

    @Transactional
    public void deleteQuestion(UserAccount admin, Long id) {
        Question question = questions.findById(id).orElseThrow(() -> new BusinessException(404, "题目不存在"));
        question.setActive(false);
        log(admin, "DISABLE_QUESTION", question.getContent());
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.RecommendationResponse> allRecommendationItems() {
        return items.findAll().stream().map(item -> DtoMapper.recommendation(item, item.getBaseScore())).toList();
    }

    @Transactional
    public ApiDtos.RecommendationResponse createRecommendationItem(UserAccount admin, ApiDtos.RecommendationItemRequest request) {
        RecommendationItem item = toItem(new RecommendationItem(), request);
        items.save(item);
        log(admin, "CREATE_RECOMMENDATION", item.getTitle());
        return DtoMapper.recommendation(item, item.getBaseScore());
    }

    @Transactional
    public ApiDtos.RecommendationResponse updateRecommendationItem(UserAccount admin, Long id, ApiDtos.RecommendationItemRequest request) {
        RecommendationItem item = items.findById(id).orElseThrow(() -> new BusinessException(404, "推荐项不存在"));
        toItem(item, request);
        log(admin, "UPDATE_RECOMMENDATION", item.getTitle());
        return DtoMapper.recommendation(item, item.getBaseScore());
    }

    @Transactional
    public void deleteRecommendationItem(UserAccount admin, Long id) {
        RecommendationItem item = items.findById(id).orElseThrow(() -> new BusinessException(404, "推荐项不存在"));
        item.setActive(false);
        log(admin, "DISABLE_RECOMMENDATION", item.getTitle());
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.AdminFeedbackResponse> feedback() {
        return feedbacks.findTop100ByOrderByCreatedAtDesc().stream()
                .map(feedback -> new ApiDtos.AdminFeedbackResponse(
                        feedback.getId(),
                        feedback.getUser().getPhone(),
                        feedback.getItem().getTitle(),
                        feedback.getItem().getScene().name().toLowerCase(),
                        feedback.getRating().name().toLowerCase(),
                        feedback.getComment(),
                        feedback.getCreatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.AdminLogResponse> logs() {
        return logs.findTop100ByOrderByCreatedAtDesc().stream()
                .map(log -> new ApiDtos.AdminLogResponse(
                        log.getId(),
                        log.getAdmin().getPhone(),
                        log.getAction(),
                        log.getDetail(),
                        log.getCreatedAt()))
                .toList();
    }

    public ApiDtos.AdminStatsResponse stats() {
        return new ApiDtos.AdminStatsResponse(
                users.count(),
                questions.count(),
                items.count(),
                feedbacks.count(),
                matches.count());
    }

    @Transactional(readOnly = true)
    public ApiDtos.AdminDashboardResponse dashboard() {
        Map<String, Long> testsByType = new LinkedHashMap<>();
        for (TestType type : TestType.values()) {
            testsByType.put(type.name().toLowerCase(), 0L);
        }
        testResults.findAll().forEach(result ->
                testsByType.compute(result.getType().name().toLowerCase(), (ignored, count) -> count == null ? 1 : count + 1));

        Map<String, Long> feedbackByRating = new LinkedHashMap<>();
        for (FeedbackRating rating : FeedbackRating.values()) {
            feedbackByRating.put(rating.name().toLowerCase(), 0L);
        }
        feedbacks.findAll().forEach(feedback ->
                feedbackByRating.compute(feedback.getRating().name().toLowerCase(), (ignored, count) -> count == null ? 1 : count + 1));

        Map<String, Long> recommendationsByScene = items.findAll().stream()
                .collect(Collectors.groupingBy(item -> item.getScene().name().toLowerCase(), LinkedHashMap::new, Collectors.counting()));
        for (SceneType scene : SceneType.values()) {
            recommendationsByScene.putIfAbsent(scene.name().toLowerCase(), 0L);
        }

        long activeShares = shares.findAll().stream().filter(share -> share.isActive()).count();
        return new ApiDtos.AdminDashboardResponse(stats(), testsByType, feedbackByRating, recommendationsByScene, activeShares);
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.AdminUserResponse> users() {
        return users.findAll().stream()
                .sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()))
                .map(DtoMapper::adminUser)
                .toList();
    }

    @Transactional
    public ApiDtos.AdminUserResponse updateUser(UserAccount admin, Long id, ApiDtos.AdminUserUpdateRequest request) {
        UserAccount user = users.findById(id).orElseThrow(() -> new BusinessException(404, "用户不存在"));
        if (request.active() != null) {
            user.setActive(request.active());
        }
        if (request.role() != null && !request.role().isBlank()) {
            user.setRole(Role.valueOf(request.role().trim().toUpperCase()));
        }
        users.save(user);
        log(admin, "UPDATE_USER", user.getPhone());
        return DtoMapper.adminUser(user);
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.RecommendationRuleResponse> recommendationRules() {
        return rules.findAllByOrderByTagAsc().stream().map(DtoMapper::recommendationRule).toList();
    }

    @Transactional
    public ApiDtos.RecommendationRuleResponse createRecommendationRule(UserAccount admin, ApiDtos.RecommendationRuleRequest request) {
        if (rules.findByTag(request.tag().trim()).isPresent()) {
            throw new BusinessException(409, "推荐规则标签已存在");
        }
        RecommendationRule rule = toRule(new RecommendationRule(), request);
        rules.save(rule);
        log(admin, "CREATE_RECOMMENDATION_RULE", rule.getTag());
        return DtoMapper.recommendationRule(rule);
    }

    @Transactional
    public ApiDtos.RecommendationRuleResponse updateRecommendationRule(UserAccount admin, Long id, ApiDtos.RecommendationRuleRequest request) {
        RecommendationRule rule = rules.findById(id).orElseThrow(() -> new BusinessException(404, "推荐规则不存在"));
        toRule(rule, request);
        log(admin, "UPDATE_RECOMMENDATION_RULE", rule.getTag());
        return DtoMapper.recommendationRule(rule);
    }

    @Transactional
    public void deleteRecommendationRule(UserAccount admin, Long id) {
        RecommendationRule rule = rules.findById(id).orElseThrow(() -> new BusinessException(404, "推荐规则不存在"));
        rules.delete(rule);
        log(admin, "DELETE_RECOMMENDATION_RULE", rule.getTag());
    }

    private Question toQuestion(Question question, ApiDtos.AdminQuestionRequest request) {
        question.setType(EnumParser.testType(request.type()));
        question.setContent(request.content());
        question.setActive(request.active() == null || request.active());
        question.setSortOrder(0);
        int index = 1;
        for (ApiDtos.AdminOptionRequest optionRequest : request.options()) {
            QuestionOption option = new QuestionOption();
            option.setLabel(optionRequest.label());
            option.setContent(optionRequest.content());
            option.setSortOrder(index++);
            option.setWeights(optionRequest.weights());
            question.addOption(option);
        }
        return question;
    }

    private RecommendationItem toItem(RecommendationItem item, ApiDtos.RecommendationItemRequest request) {
        item.setScene(EnumParser.sceneType(request.scene()));
        item.setTitle(request.title());
        item.setDescription(request.description());
        item.setTags(request.tags() == null ? List.of() : request.tags());
        item.setBaseScore(request.baseScore() == null ? 50 : request.baseScore());
        item.setActive(request.active() == null || request.active());
        return item;
    }

    private RecommendationRule toRule(RecommendationRule rule, ApiDtos.RecommendationRuleRequest request) {
        rule.setTag(request.tag().trim());
        rule.setLabel(request.label().trim());
        rule.setWeight(Math.max(-30, Math.min(30, request.weight() == null ? 0 : request.weight())));
        rule.setActive(request.active() == null || request.active());
        return rule;
    }

    private void log(UserAccount admin, String action, String detail) {
        AdminLog log = new AdminLog();
        log.setAdmin(admin);
        log.setAction(action);
        log.setDetail(detail);
        logs.save(log);
    }
}
