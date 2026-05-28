package com.personality.radar.config;

import com.personality.radar.domain.PersonalityDimension;
import com.personality.radar.domain.Question;
import com.personality.radar.domain.QuestionOption;
import com.personality.radar.domain.RecommendationItem;
import com.personality.radar.domain.RecommendationRule;
import com.personality.radar.domain.Role;
import com.personality.radar.domain.SceneType;
import com.personality.radar.domain.TestResult;
import com.personality.radar.domain.TestType;
import com.personality.radar.domain.UserAccount;
import com.personality.radar.repository.QuestionRepository;
import com.personality.radar.repository.RecommendationItemRepository;
import com.personality.radar.repository.RecommendationRuleRepository;
import com.personality.radar.repository.TestResultRepository;
import com.personality.radar.repository.UserRepository;
import java.util.List;
import java.util.Map;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements ApplicationRunner {
    private final UserRepository users;
    private final QuestionRepository questions;
    private final RecommendationItemRepository items;
    private final RecommendationRuleRepository rules;
    private final TestResultRepository results;
    private final PasswordEncoder encoder;

    public DataSeeder(
            UserRepository users,
            QuestionRepository questions,
            RecommendationItemRepository items,
            RecommendationRuleRepository rules,
            TestResultRepository results,
            PasswordEncoder encoder) {
        this.users = users;
        this.questions = questions;
        this.items = items;
        this.rules = rules;
        this.results = results;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedUsers();
        seedQuestions();
        seedRecommendationRules();
        seedRecommendations();
        seedDemoResults();
    }

    private void seedUsers() {
        if (!users.existsByPhone("13800000000")) {
            UserAccount admin = user("13800000000", "Admin@123456", "系统管理员", Role.ADMIN);
            users.save(admin);
        }
        if (!users.existsByPhone("13900000001")) {
            users.save(user("13900000001", "User123456", "晨间探索者", Role.USER));
        }
        if (!users.existsByPhone("13900000002")) {
            users.save(user("13900000002", "User123456", "安静计划家", Role.USER));
        }
    }

    private UserAccount user(String phone, String password, String name, Role role) {
        UserAccount user = new UserAccount();
        user.setPhone(phone);
        user.setPasswordHash(encoder.encode(password));
        user.setDisplayName(name);
        user.setAvatarUrl("https://api.dicebear.com/9.x/thumbs/svg?seed=" + phone);
        user.setRole(role);
        return user;
    }

    private void seedQuestions() {
        if (questions.count() > 0) {
            return;
        }
        questions.save(q(TestType.PERSONALITY, 1, "周末突然空出来一天，你更倾向于怎样安排？",
                o("A", "探索一家新展览或陌生街区", w("OPENNESS", 5, "EXTRAVERSION", 4)),
                o("B", "按计划完成整理、学习或运动", w("CONSCIENTIOUSNESS", 5)),
                o("C", "约朋友一起吃饭聊天", w("EXTRAVERSION", 5, "AGREEABLENESS", 4)),
                o("D", "在家独处恢复精力", w("NEUROTICISM", 3, "CONSCIENTIOUSNESS", 3))));
        questions.save(q(TestType.PERSONALITY, 2, "面对临时变化，你通常会怎么处理？",
                o("A", "把变化当成新的可能性", w("OPENNESS", 5)),
                o("B", "快速重排计划和优先级", w("CONSCIENTIOUSNESS", 5)),
                o("C", "先和相关的人沟通确认", w("AGREEABLENESS", 5)),
                o("D", "会有压力，需要一点时间适应", w("NEUROTICISM", 5))));
        questions.save(q(TestType.PERSONALITY, 3, "团队合作中你更常扮演什么角色？",
                o("A", "提出新点子的人", w("OPENNESS", 5)),
                o("B", "推进进度和检查细节的人", w("CONSCIENTIOUSNESS", 5)),
                o("C", "活跃气氛和表达观点的人", w("EXTRAVERSION", 5)),
                o("D", "协调关系和照顾感受的人", w("AGREEABLENESS", 5))));
        questions.save(q(TestType.PERSONALITY, 4, "当任务压力上来时，你最需要什么？",
                o("A", "一个清晰拆解后的计划", w("CONSCIENTIOUSNESS", 5)),
                o("B", "有人一起讨论和分担", w("EXTRAVERSION", 4, "AGREEABLENESS", 4)),
                o("C", "先转换场景找灵感", w("OPENNESS", 4)),
                o("D", "安静时间缓冲情绪", w("NEUROTICISM", 5))));

        questions.save(q(TestType.FOOD, 1, "选择餐厅时你最看重什么？",
                o("A", "新奇菜系和特别体验", w("OPENNESS", 5)),
                o("B", "稳定口碑和明确评价", w("CONSCIENTIOUSNESS", 5)),
                o("C", "适合多人聚餐的氛围", w("EXTRAVERSION", 5)),
                o("D", "舒适安静、服务友好", w("AGREEABLENESS", 5))));
        questions.save(q(TestType.FOOD, 2, "你更喜欢哪类饮食节奏？",
                o("A", "随兴尝鲜", w("OPENNESS", 4)),
                o("B", "规律健康", w("CONSCIENTIOUSNESS", 5)),
                o("C", "热闹分享", w("EXTRAVERSION", 4)),
                o("D", "轻负担治愈系", w("NEUROTICISM", 4))));

        questions.save(q(TestType.TRAVEL, 1, "旅行时你最期待什么？",
                o("A", "探索未知路线", w("OPENNESS", 5)),
                o("B", "完整攻略和高效行程", w("CONSCIENTIOUSNESS", 5)),
                o("C", "认识新朋友和热闹活动", w("EXTRAVERSION", 5)),
                o("D", "放松、安全、节奏舒适", w("AGREEABLENESS", 4, "NEUROTICISM", 3))));
        questions.save(q(TestType.TRAVEL, 2, "如果计划被打乱，你会更希望？",
                o("A", "临时寻找替代体验", w("OPENNESS", 5)),
                o("B", "马上恢复到备用计划", w("CONSCIENTIOUSNESS", 5)),
                o("C", "和同行者一起决定", w("AGREEABLENESS", 5)),
                o("D", "减少安排，先休息", w("NEUROTICISM", 4))));
    }

    private Question q(TestType type, int order, String content, QuestionOption... options) {
        Question question = new Question();
        question.setType(type);
        question.setSortOrder(order);
        question.setContent(content);
        int index = 1;
        for (QuestionOption option : options) {
            option.setSortOrder(index++);
            question.addOption(option);
        }
        return question;
    }

    private QuestionOption o(String label, String content, Map<String, Integer> weights) {
        QuestionOption option = new QuestionOption();
        option.setLabel(label);
        option.setContent(content);
        option.setWeights(weights);
        return option;
    }

    private Map<String, Integer> w(Object... values) {
        java.util.LinkedHashMap<String, Integer> result = new java.util.LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            result.put((String) values[i], (Integer) values[i + 1]);
        }
        for (PersonalityDimension dimension : PersonalityDimension.values()) {
            result.putIfAbsent(dimension.name(), 3);
        }
        return result;
    }

    private void seedRecommendations() {
        if (items.count() > 0) {
            return;
        }
        items.save(item(SceneType.FOOD, "创意融合小馆", "适合喜欢新鲜体验的用户，菜单变化多，适合把晚餐变成小冒险。", 68, "explore", "social"));
        items.save(item(SceneType.FOOD, "轻食能量碗", "稳定、清爽、可控的饮食方案，适合想保持节奏和效率的工作日。", 62, "structured", "gentle"));
        items.save(item(SceneType.FOOD, "热闹火锅局", "适合需要社交能量和分享氛围的朋友聚餐。", 65, "social"));
        items.save(item(SceneType.TRAVEL, "城市漫游路线", "低门槛探索街区、展览和咖啡馆，适合开放性较高的周末。", 70, "explore"));
        items.save(item(SceneType.TRAVEL, "两天一夜计划游", "交通、住宿、景点节奏清晰，适合重视确定感和效率的人。", 64, "structured"));
        items.save(item(SceneType.TRAVEL, "疗愈慢旅行", "减少打卡压力，把重点放在恢复、自然和舒适体验上。", 63, "gentle"));
        items.save(item(SceneType.OUTFIT, "清爽通勤风", "强调整洁、可靠和低出错率，适合学习、面试和日常工作。", 60, "structured"));
        items.save(item(SceneType.OUTFIT, "亮色表达风", "用颜色或配饰表达外向与探索欲，适合社交场景。", 66, "social", "explore"));
        items.save(item(SceneType.CAREER, "创意探索路径", "适合将好奇心转为项目作品，用小实验积累方向感。", 68, "explore"));
        items.save(item(SceneType.CAREER, "计划执行路径", "适合设定阶段目标、复盘指标和稳定输出节奏。", 70, "structured"));
        items.save(item(SceneType.CAREER, "协作沟通路径", "适合在团队任务中发挥关系协调与表达优势。", 64, "social", "gentle"));
    }

    private void seedRecommendationRules() {
        if (rules.count() > 0) {
            return;
        }
        rules.save(rule("explore", "探索尝鲜", 4));
        rules.save(rule("structured", "计划稳定", 4));
        rules.save(rule("social", "社交表达", 3));
        rules.save(rule("gentle", "舒缓恢复", 3));
    }

    private RecommendationRule rule(String tag, String label, int weight) {
        RecommendationRule rule = new RecommendationRule();
        rule.setTag(tag);
        rule.setLabel(label);
        rule.setWeight(weight);
        return rule;
    }

    private RecommendationItem item(SceneType scene, String title, String description, int baseScore, String... tags) {
        RecommendationItem item = new RecommendationItem();
        item.setScene(scene);
        item.setTitle(title);
        item.setDescription(description);
        item.setBaseScore(baseScore);
        item.setTags(List.of(tags));
        return item;
    }

    private void seedDemoResults() {
        UserAccount userA = users.findByPhone("13900000001").orElseThrow();
        UserAccount userB = users.findByPhone("13900000002").orElseThrow();
        if (results.findByUserOrderByCreatedAtDesc(userA).isEmpty()) {
            results.save(result(userA, TestType.PERSONALITY, 82, 66, 74, 68, 38));
            results.save(result(userA, TestType.FOOD, 78, 52, 72, 61, 35));
            results.save(result(userA, TestType.TRAVEL, 86, 58, 70, 62, 34));
        }
        if (results.findByUserOrderByCreatedAtDesc(userB).isEmpty()) {
            results.save(result(userB, TestType.PERSONALITY, 54, 84, 42, 76, 48));
            results.save(result(userB, TestType.FOOD, 48, 80, 38, 72, 46));
            results.save(result(userB, TestType.TRAVEL, 52, 82, 40, 74, 44));
        }
    }

    private TestResult result(UserAccount user, TestType type, int openness, int conscientiousness, int extraversion, int agreeableness, int neuroticism) {
        TestResult result = new TestResult();
        result.setUser(user);
        result.setType(type);
        result.setScores(Map.of(
                "OPENNESS", openness,
                "CONSCIENTIOUSNESS", conscientiousness,
                "EXTRAVERSION", extraversion,
                "AGREEABLENESS", agreeableness,
                "NEUROTICISM", neuroticism));
        return result;
    }
}
