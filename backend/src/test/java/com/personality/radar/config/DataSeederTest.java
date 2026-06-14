package com.personality.radar.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.personality.radar.domain.Question;
import com.personality.radar.domain.RecommendationItem;
import com.personality.radar.domain.RecommendationRule;
import com.personality.radar.domain.Role;
import com.personality.radar.domain.SceneType;
import com.personality.radar.domain.TestType;
import com.personality.radar.domain.TestResult;
import com.personality.radar.domain.UserAccount;
import com.personality.radar.repository.FeedbackRepository;
import com.personality.radar.repository.QuestionRepository;
import com.personality.radar.repository.RecommendationItemRepository;
import com.personality.radar.repository.RecommendationRuleRepository;
import com.personality.radar.repository.TestResultRepository;
import com.personality.radar.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

class DataSeederTest {
    @Test
    void reactivatesExistingDefaultAccountsAfterSchemaUpgrade() {
        UserAccount admin = existing("13800000000", Role.ADMIN);
        UserAccount user = existing("13900000001", Role.USER);
        UserAccount friend = existing("13900000002", Role.USER);

        UserRepository users = org.mockito.Mockito.mock(UserRepository.class);
        QuestionRepository questions = org.mockito.Mockito.mock(QuestionRepository.class);
        RecommendationItemRepository items = org.mockito.Mockito.mock(RecommendationItemRepository.class);
        RecommendationRuleRepository rules = org.mockito.Mockito.mock(RecommendationRuleRepository.class);
        TestResultRepository results = org.mockito.Mockito.mock(TestResultRepository.class);
        FeedbackRepository feedbacks = org.mockito.Mockito.mock(FeedbackRepository.class);
        PasswordEncoder encoder = org.mockito.Mockito.mock(PasswordEncoder.class);

        when(users.findByPhone("13800000000")).thenReturn(Optional.of(admin));
        when(users.findByPhone("13900000001")).thenReturn(Optional.of(user));
        when(users.findByPhone("13900000002")).thenReturn(Optional.of(friend));
        when(questions.count()).thenReturn(1L);
        when(items.count()).thenReturn(1L);
        when(rules.count()).thenReturn(1L);
        when(results.findByUserOrderByCreatedAtDesc(any(UserAccount.class))).thenReturn(List.of(new TestResult()));

        new DataSeeder(users, questions, items, rules, results, feedbacks, encoder).run(null);

        assertThat(admin.isActive()).isTrue();
        assertThat(user.isActive()).isTrue();
        assertThat(friend.isActive()).isTrue();
    }

    @Test
    void seedsCompleteReadableSampleContentForQuestionsRulesAndRecommendations() {
        UserAccount admin = existing("13800000000", Role.ADMIN);
        UserAccount user = existing("13900000001", Role.USER);
        UserAccount friend = existing("13900000002", Role.USER);

        UserRepository users = org.mockito.Mockito.mock(UserRepository.class);
        QuestionRepository questions = org.mockito.Mockito.mock(QuestionRepository.class);
        RecommendationItemRepository items = org.mockito.Mockito.mock(RecommendationItemRepository.class);
        RecommendationRuleRepository rules = org.mockito.Mockito.mock(RecommendationRuleRepository.class);
        TestResultRepository results = org.mockito.Mockito.mock(TestResultRepository.class);
        FeedbackRepository feedbacks = org.mockito.Mockito.mock(FeedbackRepository.class);
        PasswordEncoder encoder = org.mockito.Mockito.mock(PasswordEncoder.class);

        when(users.findByPhone("13800000000")).thenReturn(Optional.of(admin));
        when(users.findByPhone("13900000001")).thenReturn(Optional.of(user));
        when(users.findByPhone("13900000002")).thenReturn(Optional.of(friend));
        when(questions.count()).thenReturn(0L);
        when(items.count()).thenReturn(0L);
        when(rules.count()).thenReturn(0L);
        when(results.findByUserOrderByCreatedAtDesc(any(UserAccount.class))).thenReturn(List.of(new TestResult()));

        new DataSeeder(users, questions, items, rules, results, feedbacks, encoder).run(null);

        ArgumentCaptor<Question> questionCaptor = ArgumentCaptor.forClass(Question.class);
        ArgumentCaptor<RecommendationItem> itemCaptor = ArgumentCaptor.forClass(RecommendationItem.class);
        ArgumentCaptor<RecommendationRule> ruleCaptor = ArgumentCaptor.forClass(RecommendationRule.class);
        verify(questions, times(44)).save(questionCaptor.capture());
        verify(items, times(20)).save(itemCaptor.capture());
        verify(rules, times(4)).save(ruleCaptor.capture());

        List<Question> seededQuestions = questionCaptor.getAllValues();
        assertThat(seededQuestions).filteredOn(q -> q.getType() == TestType.PERSONALITY).hasSize(14);
        assertThat(seededQuestions).filteredOn(q -> q.getType() == TestType.FOOD).hasSize(10);
        assertThat(seededQuestions).filteredOn(q -> q.getType() == TestType.TRAVEL).hasSize(10);
        assertThat(seededQuestions).filteredOn(q -> q.getType() == TestType.SOCIAL).hasSize(10);
        assertThat(seededQuestions)
                .extracting(Question::getContent)
                .contains(
                        "看到微信群突然出现 99+ 未读时，我会想立刻点开加入聊天。",
                        "朋友带我去“超级正宗但环境很破”的苍蝇馆子时，我会更期待味道。",
                        "旅行途中遇到倾盆大雨、室外行程被打乱时，我能把它当成随机体验并调整安排。",
                        "聊天里出现抽象或发疯表情包时，我能接住并参与这种幽默。")
                .allSatisfy(content -> assertThat(content).doesNotContain("鍛", "绋", "�"));
        assertThat(seededQuestions.get(0).getOptions()).hasSize(5);
        assertThat(seededQuestions.get(0).getOptions())
                .extracting(option -> option.getContent())
                .containsExactly("非常不同意", "不太同意", "一般", "比较同意", "非常同意");
        assertThat(seededQuestions.get(0).getOptions().get(4).getWeights())
                .containsEntry("EXTRAVERSION", 5)
                .containsEntry("OPENNESS", 3)
                .containsEntry("CONSCIENTIOUSNESS", 3)
                .containsEntry("AGREEABLENESS", 3)
                .containsEntry("NEUROTICISM", 3);

        List<RecommendationItem> seededItems = itemCaptor.getAllValues();
        assertThat(seededItems).filteredOn(item -> item.getScene() == SceneType.FOOD).hasSize(4);
        assertThat(seededItems).filteredOn(item -> item.getScene() == SceneType.TRAVEL).hasSize(4);
        assertThat(seededItems).filteredOn(item -> item.getScene() == SceneType.SOCIAL).hasSize(4);
        assertThat(seededItems).filteredOn(item -> item.getScene() == SceneType.OUTFIT).hasSize(4);
        assertThat(seededItems).filteredOn(item -> item.getScene() == SceneType.CAREER).hasSize(4);
        assertThat(seededItems)
                .extracting(RecommendationItem::getTitle)
                .contains("创意融合小馆", "疗愈慢旅行", "主题沙龙或读书会", "深度专注路径")
                .allSatisfy(title -> assertThat(title).doesNotContain("鍒", "绋", "�"));

        assertThat(ruleCaptor.getAllValues())
                .extracting(RecommendationRule::getTag)
                .containsExactlyInAnyOrder("explore", "structured", "social", "gentle");
    }

    @Test
    void refreshesLegacySampleSeedWhenOldCountsAreDetected() {
        UserAccount admin = existing("13800000000", Role.ADMIN);
        UserAccount user = existing("13900000001", Role.USER);
        UserAccount friend = existing("13900000002", Role.USER);
        RecommendationRule explore = rule("explore");
        RecommendationRule structured = rule("structured");
        RecommendationRule social = rule("social");
        RecommendationRule gentle = rule("gentle");

        UserRepository users = org.mockito.Mockito.mock(UserRepository.class);
        QuestionRepository questions = org.mockito.Mockito.mock(QuestionRepository.class);
        RecommendationItemRepository items = org.mockito.Mockito.mock(RecommendationItemRepository.class);
        RecommendationRuleRepository rules = org.mockito.Mockito.mock(RecommendationRuleRepository.class);
        TestResultRepository results = org.mockito.Mockito.mock(TestResultRepository.class);
        FeedbackRepository feedbacks = org.mockito.Mockito.mock(FeedbackRepository.class);
        PasswordEncoder encoder = org.mockito.Mockito.mock(PasswordEncoder.class);

        when(users.findByPhone("13800000000")).thenReturn(Optional.of(admin));
        when(users.findByPhone("13900000001")).thenReturn(Optional.of(user));
        when(users.findByPhone("13900000002")).thenReturn(Optional.of(friend));
        when(questions.count()).thenReturn(14L);
        when(items.count()).thenReturn(16L);
        when(rules.findByTag("explore")).thenReturn(Optional.of(explore));
        when(rules.findByTag("structured")).thenReturn(Optional.of(structured));
        when(rules.findByTag("social")).thenReturn(Optional.of(social));
        when(rules.findByTag("gentle")).thenReturn(Optional.of(gentle));
        when(results.findByUserOrderByCreatedAtDesc(any(UserAccount.class))).thenReturn(List.of(new TestResult()));

        new DataSeeder(users, questions, items, rules, results, feedbacks, encoder).run(null);

        verify(feedbacks).deleteAll();
        verify(questions).deleteAll();
        verify(items).deleteAll();
        verify(questions, times(44)).save(any(Question.class));
        verify(items, times(20)).save(any(RecommendationItem.class));
        verify(rules, times(4)).save(any(RecommendationRule.class));
        assertThat(explore.getLabel()).isEqualTo("探索尝鲜");
        assertThat(structured.getLabel()).isEqualTo("计划稳定");
        assertThat(social.getLabel()).isEqualTo("社交表达");
        assertThat(gentle.getLabel()).isEqualTo("舒缓恢复");
    }

    private UserAccount existing(String phone, Role role) {
        UserAccount user = new UserAccount();
        user.setPhone(phone);
        user.setDisplayName(phone);
        user.setPasswordHash("hash");
        user.setRole(role);
        user.setActive(false);
        return user;
    }

    private RecommendationRule rule(String tag) {
        RecommendationRule rule = new RecommendationRule();
        rule.setTag(tag);
        rule.setLabel("旧标签");
        rule.setWeight(0);
        return rule;
    }
}
