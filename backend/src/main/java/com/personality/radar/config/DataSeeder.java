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
import com.personality.radar.repository.FeedbackRepository;
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
    private static final List<Long> LEGACY_SAMPLE_QUESTION_COUNTS = List.of(8L, 14L);
    private static final List<Long> LEGACY_SAMPLE_RECOMMENDATION_COUNTS = List.of(11L, 16L);

    private final UserRepository users;
    private final QuestionRepository questions;
    private final RecommendationItemRepository items;
    private final RecommendationRuleRepository rules;
    private final TestResultRepository results;
    private final FeedbackRepository feedbacks;
    private final PasswordEncoder encoder;

    public DataSeeder(
            UserRepository users,
            QuestionRepository questions,
            RecommendationItemRepository items,
            RecommendationRuleRepository rules,
            TestResultRepository results,
            FeedbackRepository feedbacks,
            PasswordEncoder encoder) {
        this.users = users;
        this.questions = questions;
        this.items = items;
        this.rules = rules;
        this.results = results;
        this.feedbacks = feedbacks;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedUsers();
        long questionCount = questions.count();
        long recommendationCount = items.count();
        boolean refreshingLegacySamples = isLegacyQuestionCount(questionCount)
                || isLegacyRecommendationCount(recommendationCount);
        seedQuestions(questionCount);
        seedRecommendationRules(refreshingLegacySamples);
        seedRecommendations(recommendationCount);
        seedDemoResults();
    }

    private boolean isLegacyQuestionCount(long count) {
        return LEGACY_SAMPLE_QUESTION_COUNTS.contains(count);
    }

    private boolean isLegacyRecommendationCount(long count) {
        return LEGACY_SAMPLE_RECOMMENDATION_COUNTS.contains(count);
    }

    private void seedUsers() {
        ensureDefaultUser("13800000000", "Admin@123456", "系统管理员", Role.ADMIN);
        ensureDefaultUser("13900000001", "User123456", "晨间探索者", Role.USER);
        ensureDefaultUser("13900000002", "User123456", "安静计划家", Role.USER);
    }

    private void ensureDefaultUser(String phone, String password, String name, Role role) {
        UserAccount existing = users.findByPhone(phone).orElse(null);
        if (existing == null) {
            users.save(user(phone, password, name, role));
            return;
        }
        existing.setActive(true);
        existing.setLockedUntil(null);
        existing.setFailedLoginAttempts(0);
        existing.setRole(role);
        users.save(existing);
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

    private void seedQuestions(long existingCount) {
        if (existingCount != 0 && !isLegacyQuestionCount(existingCount)) {
            return;
        }
        if (existingCount != 0) {
            questions.deleteAll();
        }

        Map<TestType, Integer> orders = new java.util.EnumMap<>(TestType.class);
        for (QuestionSeed seed : questionSeeds()) {
            int order = orders.merge(seed.type(), 1, Integer::sum);
            questions.save(scaleQ(seed.type(), order, seed.content(), seed.dimension(), seed.reverse()));
        }
    }

    private List<QuestionSeed> questionSeeds() {
        return List.of(
                seed(TestType.PERSONALITY, "看到微信群突然出现 99+ 未读时，我会想立刻点开加入聊天。", PersonalityDimension.EXTRAVERSION),
                seed(TestType.PERSONALITY, "面对突然通知的破冰团建，我通常会期待认识新朋友。", PersonalityDimension.EXTRAVERSION),
                seed(TestType.PERSONALITY, "线下社交和真实活动比纯线上互动更能让我进入状态。", PersonalityDimension.EXTRAVERSION),
                seed(TestType.PERSONALITY, "遇到感觉很酷、很对脾气的人时，我愿意主动打招呼。", PersonalityDimension.EXTRAVERSION),
                seed(TestType.PERSONALITY, "打游戏或做项目时，我更喜欢和队友一起组队推进。", PersonalityDimension.EXTRAVERSION),
                seed(TestType.PERSONALITY, "听到很带感的音乐时，我很容易脑补出完整故事画面。", PersonalityDimension.OPENNESS),
                seed(TestType.PERSONALITY, "复刻美食教程时，我愿意凭感觉加料，创造自己的版本。", PersonalityDimension.OPENNESS),
                seed(TestType.PERSONALITY, "睡前放空时，我的大脑经常构思各种脑洞剧情或替代方案。", PersonalityDimension.OPENNESS),
                seed(TestType.PERSONALITY, "遇到能力一般但态度很好的队友时，我愿意耐心鼓励。", PersonalityDimension.AGREEABLENESS),
                seed(TestType.PERSONALITY, "两个朋友因为小事吵起来时，我会优先安抚双方情绪。", PersonalityDimension.AGREEABLENESS),
                seed(TestType.PERSONALITY, "朋友拿着不太理想的报告找我提意见时，我会先照顾对方感受。", PersonalityDimension.AGREEABLENESS),
                seed(TestType.PERSONALITY, "出远门前，我会提前列好清单并把行李分门别类整理好。", PersonalityDimension.CONSCIENTIOUSNESS),
                seed(TestType.PERSONALITY, "拿到大作业或项目后，我会尽早拆解进度并提前完成。", PersonalityDimension.CONSCIENTIOUSNESS),
                seed(TestType.PERSONALITY, "我的电脑桌面和浏览器标签通常会保持分类清楚、数量可控。", PersonalityDimension.CONSCIENTIOUSNESS),

                seed(TestType.FOOD, "朋友带我去“超级正宗但环境很破”的苍蝇馆子时，我会更期待味道。", PersonalityDimension.OPENNESS),
                seed(TestType.FOOD, "深夜赶工时，我会因为担心负担而尽量忍住不吃高热量夜宵。", PersonalityDimension.NEUROTICISM),
                seed(TestType.FOOD, "遇到折耳根、香菜、螺蛳粉这类争议食物时，我愿意尝试一下。", PersonalityDimension.OPENNESS),
                reverseSeed(TestType.FOOD, "一个人吃火锅或烤肉时，我也能自在享受，不太在意旁人眼光。", PersonalityDimension.NEUROTICISM),
                seed(TestType.FOOD, "遇到精美的大餐时，我会想先拍照记录或分享给朋友。", PersonalityDimension.EXTRAVERSION),
                seed(TestType.FOOD, "点饮品时，我愿意主动调整糖度冰度来匹配当天状态。", PersonalityDimension.CONSCIENTIOUSNESS),
                seed(TestType.FOOD, "聚餐时，我很在意公共餐桌礼仪和彼此尊重。", PersonalityDimension.AGREEABLENESS),
                seed(TestType.FOOD, "朋友请客选餐厅时，我愿意明确表达自己能接受的口味底线。", PersonalityDimension.CONSCIENTIOUSNESS),
                seed(TestType.FOOD, "看到奇特跨界融合菜的新店时，我愿意拉朋友去尝鲜。", PersonalityDimension.OPENNESS),
                seed(TestType.FOOD, "累了一天后，重口味、高能量的食物更容易让我恢复状态。", PersonalityDimension.EXTRAVERSION),

                seed(TestType.TRAVEL, "面对长假目的地，我会被异国、边疆或文化差异大的地方吸引。", PersonalityDimension.OPENNESS),
                reverseSeed(TestType.TRAVEL, "规划旅行时，我更倾向选择熟悉、安全、去过也不踩雷的地方。", PersonalityDimension.OPENNESS),
                seed(TestType.TRAVEL, "出门旅游时，我期待科技馆、极限运动或特色民宿这类有记忆点的项目。", PersonalityDimension.OPENNESS),
                seed(TestType.TRAVEL, "去热门城市时，我愿意钻进本地社区和菜市场体验真实生活。", PersonalityDimension.OPENNESS),
                seed(TestType.TRAVEL, "去陌生城市前，我会先用地图或攻略熟悉地形和路线。", PersonalityDimension.CONSCIENTIOUSNESS),
                seed(TestType.TRAVEL, "旅行遇到音乐节、赛事或大型活动时，我愿意去现场感受氛围。", PersonalityDimension.EXTRAVERSION),
                reverseSeed(TestType.TRAVEL, "团队比赛后有空余时间时，我更想直接回酒店安静恢复。", PersonalityDimension.EXTRAVERSION),
                seed(TestType.TRAVEL, "抵达旅游目的地后，我会先确认吃饭、拍照或休整这些关键需求。", PersonalityDimension.CONSCIENTIOUSNESS),
                reverseSeed(TestType.TRAVEL, "旅行途中遇到倾盆大雨、室外行程被打乱时，我能把它当成随机体验并调整安排。", PersonalityDimension.NEUROTICISM),
                seed(TestType.TRAVEL, "如果目的地很远或机会难得，我可以接受短时间高密度打卡。", PersonalityDimension.OPENNESS),

                seed(TestType.SOCIAL, "在线下活动遇到很对脾气的人时，我愿意主动加联系方式。", PersonalityDimension.EXTRAVERSION),
                seed(TestType.SOCIAL, "打游戏时，我更喜欢拉朋友组队，而不是一直单排。", PersonalityDimension.EXTRAVERSION),
                seed(TestType.SOCIAL, "朋友发生争执时，我会努力安抚双方情绪，降低冲突。", PersonalityDimension.AGREEABLENESS),
                seed(TestType.SOCIAL, "聊天里出现抽象或发疯表情包时，我能接住并参与这种幽默。", PersonalityDimension.OPENNESS),
                seed(TestType.SOCIAL, "组队比赛或项目中，我愿意担任主动推进和带节奏的人。", PersonalityDimension.EXTRAVERSION),
                seed(TestType.SOCIAL, "面对突然的过度热情，我会立刻高度警觉，很难放松互动。", PersonalityDimension.NEUROTICISM),
                reverseSeed(TestType.SOCIAL, "发生严重分歧时，我可以先冷静下来，再找合适时机沟通。", PersonalityDimension.NEUROTICISM),
                seed(TestType.SOCIAL, "和朋友一起安排活动时，我愿意提前确认预算、时间和路线。", PersonalityDimension.CONSCIENTIOUSNESS),
                seed(TestType.SOCIAL, "别人临时迟到或改变计划时，我愿意先理解原因再处理安排。", PersonalityDimension.AGREEABLENESS),
                seed(TestType.SOCIAL, "我愿意认识不同背景的新朋友，并尝试对方熟悉的活动方式。", PersonalityDimension.OPENNESS));
    }

    private QuestionSeed seed(TestType type, String content, PersonalityDimension dimension) {
        return new QuestionSeed(type, content, dimension, false);
    }

    private QuestionSeed reverseSeed(TestType type, String content, PersonalityDimension dimension) {
        return new QuestionSeed(type, content, dimension, true);
    }

    private Question scaleQ(TestType type, int order, String content, PersonalityDimension dimension, boolean reverse) {
        Question question = new Question();
        question.setType(type);
        question.setSortOrder(order);
        question.setContent(content);
        question.addOption(scaleOption("1", "非常不同意", 1, dimension, reverse));
        question.addOption(scaleOption("2", "不太同意", 2, dimension, reverse));
        question.addOption(scaleOption("3", "一般", 3, dimension, reverse));
        question.addOption(scaleOption("4", "比较同意", 4, dimension, reverse));
        question.addOption(scaleOption("5", "非常同意", 5, dimension, reverse));
        return question;
    }

    private QuestionOption scaleOption(String label, String content, int value, PersonalityDimension dimension, boolean reverse) {
        QuestionOption option = new QuestionOption();
        option.setLabel(label);
        option.setContent(content);
        option.setWeights(w(dimension.name(), reverse ? 6 - value : value));
        return option;
    }

    private record QuestionSeed(TestType type, String content, PersonalityDimension dimension, boolean reverse) {
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

    private void seedRecommendations(long existingCount) {
        if (existingCount != 0 && !isLegacyRecommendationCount(existingCount)) {
            return;
        }
        if (existingCount != 0) {
            feedbacks.deleteAll();
            items.deleteAll();
        }
        items.save(item(SceneType.FOOD, "创意融合小馆", "适合喜欢新鲜体验的人，菜单变化多，适合把晚餐变成一次小冒险。", 68, "explore", "social"));
        items.save(item(SceneType.FOOD, "轻食能量碗", "稳定、清爽、可控，适合想保持节奏和效率的工作日。", 62, "structured", "gentle"));
        items.save(item(SceneType.FOOD, "热闹火锅局", "适合需要社交能量和分享氛围的朋友聚餐。", 65, "social"));
        items.save(item(SceneType.FOOD, "安静日式定食", "选择明确、环境克制，适合低压力用餐。", 64, "gentle", "structured"));
        items.save(item(SceneType.TRAVEL, "城市漫游路线", "低门槛探索街区、展览和咖啡馆，适合开放性较高的周末。", 70, "explore"));
        items.save(item(SceneType.TRAVEL, "两天一夜计划游", "交通、住宿、景点节奏清晰，适合重视确定感和效率的人。", 64, "structured"));
        items.save(item(SceneType.TRAVEL, "市集与夜游路线", "适合喜欢人群、活动和即时交流的旅行者。", 66, "social", "explore"));
        items.save(item(SceneType.TRAVEL, "疗愈慢旅行", "减少打卡压力，把重点放在恢复、自然和舒适体验。", 63, "gentle"));
        items.save(item(SceneType.SOCIAL, "主题沙龙或读书会", "适合在低压力场景里认识同频的人，并保留表达空间。", 67, "explore", "gentle"));
        items.save(item(SceneType.SOCIAL, "市集与轻社交活动", "人群、摊位和活动都足够开放，适合边走边聊。", 68, "social", "explore"));
        items.save(item(SceneType.SOCIAL, "三人以内的小聚", "低噪音、关系稳定，适合需要恢复又不想完全独处的时刻。", 66, "gentle", "social"));
        items.save(item(SceneType.SOCIAL, "协作体验工作坊", "用共同任务打开交流，适合把新朋友互动变得更自然。", 65, "structured", "social"));
        items.save(item(SceneType.OUTFIT, "清爽通勤风", "强调整洁、可靠和低出错率，适合学习、面试和日常工作。", 60, "structured"));
        items.save(item(SceneType.OUTFIT, "亮色表达风", "用颜色或配饰表达外向与探索欲，适合社交场景。", 66, "social", "explore"));
        items.save(item(SceneType.OUTFIT, "柔和舒适风", "材质柔软、色彩低刺激，适合需要稳定状态的日子。", 61, "gentle"));
        items.save(item(SceneType.OUTFIT, "功能胶囊衣橱", "少量单品覆盖多场景，适合重视效率和秩序的人。", 65, "structured", "gentle"));
        items.save(item(SceneType.CAREER, "创意探索路径", "适合把好奇心转为项目作品，用小实验积累方向感。", 68, "explore"));
        items.save(item(SceneType.CAREER, "计划执行路径", "适合设定阶段目标、复盘指标和稳定输出节奏。", 70, "structured"));
        items.save(item(SceneType.CAREER, "协作沟通路径", "适合在团队任务中发挥关系协调与表达优势。", 64, "social", "gentle"));
        items.save(item(SceneType.CAREER, "深度专注路径", "适合减少干扰，围绕一个主题持续积累能力。", 66, "structured", "gentle"));
    }

    private void seedRecommendationRules(boolean refreshingLegacySamples) {
        if (!refreshingLegacySamples && rules.count() > 0) {
            return;
        }
        saveRule("explore", "探索尝鲜", 4);
        saveRule("structured", "计划稳定", 4);
        saveRule("social", "社交表达", 3);
        saveRule("gentle", "舒缓恢复", 3);
    }

    private void saveRule(String tag, String label, int weight) {
        RecommendationRule rule = rules.findByTag(tag).orElseGet(() -> {
            RecommendationRule created = new RecommendationRule();
            created.setTag(tag);
            return created;
        });
        rule.setLabel(label);
        rule.setWeight(weight);
        rule.setActive(true);
        rules.save(rule);
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
            results.save(result(userA, TestType.SOCIAL, 76, 64, 82, 72, 36));
        }
        if (results.findByUserOrderByCreatedAtDesc(userB).isEmpty()) {
            results.save(result(userB, TestType.PERSONALITY, 54, 84, 42, 76, 48));
            results.save(result(userB, TestType.FOOD, 48, 80, 38, 72, 46));
            results.save(result(userB, TestType.TRAVEL, 52, 82, 40, 74, 44));
            results.save(result(userB, TestType.SOCIAL, 58, 78, 46, 82, 42));
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
