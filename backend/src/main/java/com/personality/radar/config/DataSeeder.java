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
    private static final List<Long> LEGACY_SAMPLE_QUESTION_COUNTS = List.of(8L, 14L, 44L);
    private static final List<Long> LEGACY_SAMPLE_RECOMMENDATION_COUNTS = List.of(11L, 16L, 20L);

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
            questions.save(scaleQ(seed.type(), order, seed.content(), seed.dimension(), seed.secondary(), seed.reverse()));
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

                seed(TestType.FOOD, "朋友带我去“超级正宗但环境很破”的苍蝇馆子时，我会更期待味道。", PersonalityDimension.OPENNESS, PersonalityDimension.FOOD_ADVENTURE),
                seed(TestType.FOOD, "深夜赶工时，我会因为担心负担而尽量忍住不吃高热量夜宵。", PersonalityDimension.NEUROTICISM, PersonalityDimension.FOOD_ADVENTURE),
                seed(TestType.FOOD, "遇到折耳根、香菜、蟺蜓粉这类争议食物时，我愿意尝试一下。", PersonalityDimension.OPENNESS, PersonalityDimension.FOOD_ADVENTURE),
                reverseSeed(TestType.FOOD, "一个人吃火锅或烤肉时，我也能自在享受，不太在意旁人眼光。", PersonalityDimension.NEUROTICISM, PersonalityDimension.FOOD_SOCIAL),
                seed(TestType.FOOD, "遇到精美的大餐时，我会想先拍照记录或分享给朋友。", PersonalityDimension.EXTRAVERSION, PersonalityDimension.FOOD_SOCIAL),
                seed(TestType.FOOD, "点饮品时，我愿意主动调整糖度冰度来匹配当天状态。", PersonalityDimension.CONSCIENTIOUSNESS, PersonalityDimension.FOOD_ADVENTURE),
                seed(TestType.FOOD, "聚餐时，我很在意公共餐桌礼仪和彼此尊重。", PersonalityDimension.AGREEABLENESS, PersonalityDimension.FOOD_SOCIAL),
                seed(TestType.FOOD, "朋友请客选餐厅时，我愿意明确表达自己能接受的口味底线。", PersonalityDimension.CONSCIENTIOUSNESS, PersonalityDimension.FOOD_ADVENTURE),
                seed(TestType.FOOD, "看到奇特跨界融合菜的新店时，我愿意拉朋友去尝鲜。", PersonalityDimension.OPENNESS, PersonalityDimension.FOOD_ADVENTURE),
                seed(TestType.FOOD, "累了一天后，重口味、高能量的食物更容易让我恢复状态。", PersonalityDimension.EXTRAVERSION, PersonalityDimension.FOOD_ADVENTURE),

                seed(TestType.TRAVEL, "面对长假目的地，我会被异国、边疆或文化差异大的地方吸引。", PersonalityDimension.OPENNESS, PersonalityDimension.TRAVEL_ADVENTURE),
                reverseSeed(TestType.TRAVEL, "规划旅行时，我更倾向选择熟悉、安全、去过也不踩雷的地方。", PersonalityDimension.OPENNESS, PersonalityDimension.TRAVEL_ADVENTURE),
                seed(TestType.TRAVEL, "出门旅游时，我期待科技馆、极限运动或特色民宿这类有记忆点的项目。", PersonalityDimension.OPENNESS, PersonalityDimension.TRAVEL_ADVENTURE),
                seed(TestType.TRAVEL, "去热门城市时，我愿意钻进本地社区和菜市场体验真实生活。", PersonalityDimension.OPENNESS, PersonalityDimension.TRAVEL_ADVENTURE),
                seed(TestType.TRAVEL, "去陌生城市前，我会先用地图或攻略熟悉地形和路线。", PersonalityDimension.CONSCIENTIOUSNESS, PersonalityDimension.TRAVEL_PLANNING),
                seed(TestType.TRAVEL, "旅行遇到音乐节、赛事或大型活动时，我愿意去现场感受氛围。", PersonalityDimension.EXTRAVERSION, PersonalityDimension.TRAVEL_ADVENTURE),
                reverseSeed(TestType.TRAVEL, "团队比赛后有空余时间时，我更想直接回酒店安静恢复。", PersonalityDimension.EXTRAVERSION, PersonalityDimension.TRAVEL_ADVENTURE),
                seed(TestType.TRAVEL, "抵达旅游目的地后，我会先确认吃饭、拍照或休整这些关键需求。", PersonalityDimension.CONSCIENTIOUSNESS, PersonalityDimension.TRAVEL_PLANNING),
                reverseSeed(TestType.TRAVEL, "旅行途中遇到倾盆大雨、室外行程被打乱时，我能把它当成随机体验并调整安排。", PersonalityDimension.NEUROTICISM, PersonalityDimension.TRAVEL_ADVENTURE),
                seed(TestType.TRAVEL, "如果目的地很远或机会难得，我可以接受短时间高密度打卡。", PersonalityDimension.OPENNESS, PersonalityDimension.TRAVEL_ADVENTURE),

                seed(TestType.SOCIAL, "在线下活动遇到很对脾气的人时，我愿意主动加联系方式。", PersonalityDimension.EXTRAVERSION, PersonalityDimension.SOCIAL_ENERGY),
                seed(TestType.SOCIAL, "打游戏时，我更喜欢拉朋友组队，而不是一直单排。", PersonalityDimension.EXTRAVERSION, PersonalityDimension.SOCIAL_ENERGY),
                seed(TestType.SOCIAL, "朋友发生争执时，我会努力安抚双方情绪，降低冲突。", PersonalityDimension.AGREEABLENESS, PersonalityDimension.SOCIAL_ENERGY),
                seed(TestType.SOCIAL, "聊天里出现抽象或发疯表情包时，我能接住并参与这种幽默。", PersonalityDimension.OPENNESS, PersonalityDimension.SOCIAL_ENERGY),
                seed(TestType.SOCIAL, "组队比赛或项目中，我愿意担任主动推进和带节奏的人。", PersonalityDimension.EXTRAVERSION, PersonalityDimension.SOCIAL_ENERGY),
                seed(TestType.SOCIAL, "面对突然的过度热情，我会立刻高度警觉，很难放松互动。", PersonalityDimension.NEUROTICISM, PersonalityDimension.SOCIAL_ENERGY),
                reverseSeed(TestType.SOCIAL, "发生严重分歧时，我可以先冷静下来，再找合适时机沟通。", PersonalityDimension.NEUROTICISM, PersonalityDimension.SOCIAL_ENERGY),
                seed(TestType.SOCIAL, "和朋友一起安排活动时，我愿意提前确认预算、时间和路线。", PersonalityDimension.CONSCIENTIOUSNESS, PersonalityDimension.SOCIAL_ENERGY),
                seed(TestType.SOCIAL, "别人临时迟到或改变计划时，我愿意先理解原因再处理安排。", PersonalityDimension.AGREEABLENESS, PersonalityDimension.SOCIAL_ENERGY),
                seed(TestType.SOCIAL, "我愿意认识不同背景的新朋友，并尝试对方熟悉的活动方式。", PersonalityDimension.OPENNESS, PersonalityDimension.SOCIAL_ENERGY));
    }

    private QuestionSeed seed(TestType type, String content, PersonalityDimension dimension) {
        return new QuestionSeed(type, content, dimension, null, false);
    }

    private QuestionSeed seed(TestType type, String content, PersonalityDimension dimension, PersonalityDimension secondary) {
        return new QuestionSeed(type, content, dimension, secondary, false);
    }

    private QuestionSeed reverseSeed(TestType type, String content, PersonalityDimension dimension) {
        return new QuestionSeed(type, content, dimension, null, true);
    }

    private QuestionSeed reverseSeed(TestType type, String content, PersonalityDimension dimension, PersonalityDimension secondary) {
        return new QuestionSeed(type, content, dimension, secondary, true);
    }

    private Question scaleQ(TestType type, int order, String content, PersonalityDimension dimension, boolean reverse) {
        return scaleQ(type, order, content, dimension, null, reverse);
    }

    private Question scaleQ(TestType type, int order, String content, PersonalityDimension dimension,
                            PersonalityDimension secondary, boolean reverse) {
        Question question = new Question();
        question.setType(type);
        question.setSortOrder(order);
        question.setContent(content);
        question.addOption(scaleOption("1", "非常不同意", 1, dimension, secondary, reverse));
        question.addOption(scaleOption("2", "不太同意", 2, dimension, secondary, reverse));
        question.addOption(scaleOption("3", "一般", 3, dimension, secondary, reverse));
        question.addOption(scaleOption("4", "比较同意", 4, dimension, secondary, reverse));
        question.addOption(scaleOption("5", "非常同意", 5, dimension, secondary, reverse));
        return question;
    }

    private QuestionOption scaleOption(String label, String content, int value,
                                       PersonalityDimension dimension, PersonalityDimension secondary, boolean reverse) {
        QuestionOption option = new QuestionOption();
        option.setLabel(label);
        option.setContent(content);
        int mapped = reverse ? 6 - value : value;
        if (secondary != null) {
            option.setWeights(w(dimension.name(), mapped, secondary.name(), mapped));
        } else {
            option.setWeights(w(dimension.name(), mapped));
        }
        return option;
    }

    private record QuestionSeed(TestType type, String content, PersonalityDimension dimension,
                                PersonalityDimension secondary, boolean reverse) {
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
        // FOOD: 30 items
        items.save(item(SceneType.FOOD, "创意融合小馆", "菜单变化多，把晚餐变成轻量冒险。", 68, "explore", "social"));
        items.save(item(SceneType.FOOD, "轻食能量碗", "清爽、可控，适合保持节奏的工作日。", 62, "structured", "gentle"));
        items.save(item(SceneType.FOOD, "热闹火锅局", "需要社交能量和分享氛围的朋友聚餐。", 65, "social"));
        items.save(item(SceneType.FOOD, "安静日式定食", "选择明确、环境克制，低压力用餐。", 64, "gentle", "structured"));
        items.save(item(SceneType.FOOD, "深夜烧烤摊", "烟火气十足，适合放松心情的夜宵场景。", 63, "social", "explore"));
        items.save(item(SceneType.FOOD, "精品咖啡店", "安静、品质稳定，适合独处或小型讨论。", 67, "gentle", "structured"));
        items.save(item(SceneType.FOOD, "东南亚风味餐厅", "酸辣鲜香，适合想换口味的探索者。", 69, "explore"));
        items.save(item(SceneType.FOOD, "家庭式私房菜", "温聲、亲切，适合小范围深度交流。", 66, "gentle", "social"));
        items.save(item(SceneType.FOOD, "自助海鲜大餐", "自由选择、丰俭由人，满足掌控感和探索欲。", 64, "structured", "explore"));
        items.save(item(SceneType.FOOD, "网红甜品店", "颜值和口味兼备，适合拍照分享。", 67, "social", "explore"));
        items.save(item(SceneType.FOOD, "素食轻食馆", "健康、环保，适合关注身体状态的人。", 61, "gentle"));
        items.save(item(SceneType.FOOD, "快餐汉堡店", "高效、标准，适合时间紧张的时刻。", 60, "structured"));
        items.save(item(SceneType.FOOD, "川湘辣味馆", "重口味刺激，适合需要释放压力的场景。", 66, "explore", "social"));
        items.save(item(SceneType.FOOD, "日式拉面店", "一人食友好，效率与美味兼顾。", 64, "structured", "gentle"));
        items.save(item(SceneType.FOOD, "意大利小馆", "浪漫悠闲，适合二人约会或独自享受。", 68, "explore", "gentle"));
        items.save(item(SceneType.FOOD, "早午餐店", "周末慢时光，适合不赶时间的放松。", 65, "gentle", "social"));
        items.save(item(SceneType.FOOD, "夜市小吃街", "边走边吃，适合爱热闹的探索型吃货。", 70, "explore", "social"));
        items.save(item(SceneType.FOOD, "高端法餐厅", "仪式感强，适合特殊纪念日和精致体验。", 63, "structured", "gentle"));
        items.save(item(SceneType.FOOD, "农家乐土菜馆", "返璞归真，适合向往自然和朴实风味的人。", 62, "gentle", "explore"));
        items.save(item(SceneType.FOOD, "日式居酒屋", "小酌配小食，轻松社交不负担。", 67, "social", "gentle"));
        items.save(item(SceneType.FOOD, "韩式炸鸡店", "聚会标配，分享型快乐。", 65, "social"));
        items.save(item(SceneType.FOOD, "健康沙拉吧", "精准营养，适合有健身和健康目标的人。", 60, "structured"));
        items.save(item(SceneType.FOOD, "潮汕牛肉火锅", "新鲜现切，精致与热闹并存。", 68, "explore", "social"));
        items.save(item(SceneType.FOOD, "兰州拉面馆", "快捷、实惠、稳定，日常工作餐首选。", 61, "structured", "gentle"));
        items.save(item(SceneType.FOOD, "茶餐厅", "中西融合，选择多、节奏快。", 64, "explore", "structured"));
        items.save(item(SceneType.FOOD, "分子料理体验", "前卫新奇，适合极致的探索精神。", 72, "explore"));
        items.save(item(SceneType.FOOD, "社区食堂", "家常味道、价格亲民，稳定安心。", 60, "gentle", "structured"));
        items.save(item(SceneType.FOOD, "露营野餐", "自己动手、户外享受，兼具社交与探索。", 69, "explore", "social"));
        items.save(item(SceneType.FOOD, "素食自助", "多选择、轻负担，适合饮食自律者。", 62, "structured", "gentle"));
        items.save(item(SceneType.FOOD, "小吃连锁", "快速解决，标准化口味不出错。", 59, "structured"));
        // TRAVEL: 30 items
        items.save(item(SceneType.TRAVEL, "城市漫游路线", "街区、展览和咖啡馆串起不赶路的周末。", 70, "explore"));
        items.save(item(SceneType.TRAVEL, "两天一夜计划游", "交通住宿节奏清晰，降低临场决策压力。", 64, "structured"));
        items.save(item(SceneType.TRAVEL, "市集与夜游路线", "适合喜欢人群和即时交流的旅行者。", 66, "social", "explore"));
        items.save(item(SceneType.TRAVEL, "疗愈慢旅行", "减少打卡压力，重点在恢复和舒适体验。", 63, "gentle"));
        items.save(item(SceneType.TRAVEL, "自驾环线游", "自由度高，适合喜欢掌控节奏的旅行者。", 68, "structured", "explore"));
        items.save(item(SceneType.TRAVEL, "背包客穷游", "低成本深度体验，适合冒险精神。", 72, "explore"));
        items.save(item(SceneType.TRAVEL, "豪华度假村", "一站式放松，不需计划和操心。", 62, "gentle", "structured"));
        items.save(item(SceneType.TRAVEL, "文化遗址探访", "沉浸历史，适合有求知欲的探索者。", 69, "explore", "gentle"));
        items.save(item(SceneType.TRAVEL, "主题乐园狂欢", "释放童心，适合社交能量充沛的人。", 67, "social", "explore"));
        items.save(item(SceneType.TRAVEL, "温泉之旅", "身心放松，适合需要恢复的节奏。", 64, "gentle"));
        items.save(item(SceneType.TRAVEL, "摄影旅拍", "用镜头记录，适合有审美追求的人。", 66, "explore", "structured"));
        items.save(item(SceneType.TRAVEL, "古镇慢生活", "远离喧嚣，低刺激慢节奏。", 63, "gentle", "social"));
        items.save(item(SceneType.TRAVEL, "滑雪度假", "刺激与放松结合，适合冬季爱好者。", 70, "explore", "social"));
        items.save(item(SceneType.TRAVEL, "海岛浮潜", "探索海底世界，新鲜感满分。", 71, "explore"));
        items.save(item(SceneType.TRAVEL, "跟团精品游", "省心省力，一切安排妥当。", 61, "structured", "gentle"));
        items.save(item(SceneType.TRAVEL, "骑行路线挑战", "身体力行，适合计划型运动者。", 67, "structured", "explore"));
        items.save(item(SceneType.TRAVEL, "音乐节之旅", "以演出为目的地，社交与探索并存。", 68, "social", "explore"));
        items.save(item(SceneType.TRAVEL, "美食寻访之旅", "以吃为线索，适合饮食探索爱好者。", 69, "explore", "social"));
        items.save(item(SceneType.TRAVEL, "徒步登山", "计划与毅力，适合目标明确的行动者。", 65, "structured"));
        items.save(item(SceneType.TRAVEL, "民宿隐居", "安静独处，适合需恢复能量的时刻。", 62, "gentle"));
        items.save(item(SceneType.TRAVEL, "城市夜景游", "灯光、江景、夜色中的社交。", 66, "social", "gentle"));
        items.save(item(SceneType.TRAVEL, "博物馆之旅", "系统化知识获取，适合深度思考者。", 64, "structured", "explore"));
        items.save(item(SceneType.TRAVEL, "边境跨境游", "异国风情，适合高开放性探索者。", 73, "explore"));
        items.save(item(SceneType.TRAVEL, "亲子乐园游", "家庭为中心，温馨安全优先。", 60, "gentle", "structured"));
        items.save(item(SceneType.TRAVEL, "极限运动体验", "跳伞蹦极，极端探索与刺激。", 74, "explore"));
        items.save(item(SceneType.TRAVEL, "禅修静心之旅", "放空、内省，适合情绪敏感需恢复。", 61, "gentle"));
        items.save(item(SceneType.TRAVEL, "邮轮之旅", "一站式社交与探索，节奏舒适。", 65, "social", "gentle"));
        items.save(item(SceneType.TRAVEL, "自驾露营", "自由探索与计划结合。", 67, "structured", "explore"));
        items.save(item(SceneType.TRAVEL, "志愿者旅行", "用行动连接世界，适合高宜人性。", 63, "gentle", "social"));
        items.save(item(SceneType.TRAVEL, "商务快捷出行", "高效率、低冗余，适合忙碌节奏。", 59, "structured"));
        // SOCIAL: 30 items
        items.save(item(SceneType.SOCIAL, "主题沙龙或读书会", "低压力场景里认识同频的人。", 67, "explore", "gentle"));
        items.save(item(SceneType.SOCIAL, "市集与轻社交活动", "人群、摊位足够开放，边走边聊。", 68, "social", "explore"));
        items.save(item(SceneType.SOCIAL, "三人以内的小聚", "低噪音、关系稳定，不想完全独处。", 66, "gentle", "social"));
        items.save(item(SceneType.SOCIAL, "协作体验工作坊", "共同任务打开交流，互动更自然。", 65, "structured", "social"));
        items.save(item(SceneType.SOCIAL, "桌游之夜", "规则明确、互动丰富，社交无压力。", 69, "social", "structured"));
        items.save(item(SceneType.SOCIAL, "户外徒步组队", "边走边聊，低强度社交。", 64, "social", "explore"));
        items.save(item(SceneType.SOCIAL, "线上游戏开黑", "远程社交，适合需要控制社交节奏的人。", 66, "social", "gentle"));
        items.save(item(SceneType.SOCIAL, "志愿者活动", "以服务连接他人，适合高宜人性。", 63, "gentle", "social"));
        items.save(item(SceneType.SOCIAL, "电影观影会", "安静共享体验，低互动需求。", 62, "gentle"));
        items.save(item(SceneType.SOCIAL, "KTV欢唱夜", "释放压力、热闹互动，高社交能量。", 68, "social"));
        items.save(item(SceneType.SOCIAL, "咖啡馆独处", "不需社交的社交，在人群中独处。", 61, "gentle", "explore"));
        items.save(item(SceneType.SOCIAL, "羽毛球/运动约局", "有规则、有互动，社交节奏可控。", 65, "structured", "social"));
        items.save(item(SceneType.SOCIAL, "读书俱乐部", "深度交流、观点碰撞。", 67, "explore", "gentle"));
        items.save(item(SceneType.SOCIAL, "即兴戏剧工作坊", "打开自我、高探索高社交。", 71, "explore", "social"));
        items.save(item(SceneType.SOCIAL, "亲子互动活动", "以家庭为中心的安全社交。", 60, "gentle"));
        items.save(item(SceneType.SOCIAL, "行业交流酒会", "专业社交、目标明确。", 66, "structured", "social"));
        items.save(item(SceneType.SOCIAL, "独立书店闲逛", "安静、自由，适合低社交需求。", 62, "gentle", "explore"));
        items.save(item(SceneType.SOCIAL, "DIY手作课程", "创作中自然交流，不刻意的社交。", 68, "explore", "gentle"));
        items.save(item(SceneType.SOCIAL, "宠物聚会", "以宠物为媒介的轻松社交。", 67, "social", "gentle"));
        items.save(item(SceneType.SOCIAL, "线上兴趣社群", "按兴趣分组，社交有边界。", 64, "structured", "social"));
        items.save(item(SceneType.SOCIAL, "博物馆导览", "安静、有序的轻度社交。", 63, "gentle", "explore"));
        items.save(item(SceneType.SOCIAL, "电竞赛事观赛", "集体观看、共享激情。", 69, "social"));
        items.save(item(SceneType.SOCIAL, "公益义卖活动", "贡献与连接，适合高责任感。", 64, "gentle", "structured"));
        items.save(item(SceneType.SOCIAL, "舞蹈体验课", "用身体表达，打破社交壁垒。", 70, "explore", "social"));
        items.save(item(SceneType.SOCIAL, "写作互助小组", "深度表达、互相反馈。", 65, "explore", "gentle"));
        items.save(item(SceneType.SOCIAL, "阳台花园打理", "独处享受，零社交压力。", 60, "gentle"));
        items.save(item(SceneType.SOCIAL, "烹饪分享会", "以美食为媒介的自然社交。", 67, "social", "explore"));
        items.save(item(SceneType.SOCIAL, "摄影外拍活动", "共同创作、互拍互学。", 66, "explore", "social"));
        items.save(item(SceneType.SOCIAL, "冥想共修", "安静共处，低刺激社交。", 61, "gentle"));
        items.save(item(SceneType.SOCIAL, "辩论赛/演讲俱乐部", "高强度表达，适合自信社交者。", 68, "structured", "social"));
        // OUTFIT: 30 items
        items.save(item(SceneType.OUTFIT, "清爽通勤风", "整洁、可靠、低出错率，适合工作学习。", 60, "structured"));
        items.save(item(SceneType.OUTFIT, "亮色表达风", "颜色配饰表达外向与探索欲。", 66, "social", "explore"));
        items.save(item(SceneType.OUTFIT, "柔和舒适风", "材质柔软、色彩低刺激。", 61, "gentle"));
        items.save(item(SceneType.OUTFIT, "功能胶囊衣橱", "少量单品覆盖多场景，效率优先。", 65, "structured", "gentle"));
        items.save(item(SceneType.OUTFIT, "复古文艺风", "独特审美，适合高开放性的人。", 68, "explore"));
        items.save(item(SceneType.OUTFIT, "运动休闲风", "舒适实用，兼顾活力与放松。", 63, "social", "gentle"));
        items.save(item(SceneType.OUTFIT, "极简黑白灰", "不费脑、不出错，适合重视效率。", 60, "structured", "gentle"));
        items.save(item(SceneType.OUTFIT, "潮流街头风", "大胆、张扬，适合高社交能量者。", 69, "social", "explore"));
        items.save(item(SceneType.OUTFIT, "日系森女风", "自然、柔和，适合低刺激偏好。", 62, "gentle", "explore"));
        items.save(item(SceneType.OUTFIT, "商务正装", "场合明确、规则清晰。", 61, "structured"));
        items.save(item(SceneType.OUTFIT, "波西米亚风", "自由、浪漫，适合探索型灵魂。", 67, "explore", "gentle"));
        items.save(item(SceneType.OUTFIT, "学院风", "温文尔雅、书卷气，稳定不出格。", 64, "gentle", "structured"));
        items.save(item(SceneType.OUTFIT, "暗黑系穿搭", "个性强烈、不拘一格。", 70, "explore", "social"));
        items.save(item(SceneType.OUTFIT, "户外机能风", "实用与时尚结合，适合计划型行动者。", 65, "structured", "explore"));
        items.save(item(SceneType.OUTFIT, "法式优雅", "精致不费力，适合社交场景。", 66, "social", "gentle"));
        items.save(item(SceneType.OUTFIT, "居家舒适服", "独处或低互动场景的最佳选择。", 59, "gentle"));
        items.save(item(SceneType.OUTFIT, "瑜伽运动装", "健康生活方式的表达。", 62, "structured", "gentle"));
        items.save(item(SceneType.OUTFIT, "高街混搭", "快时尚与奢侈品的碰撞，前卫探索。", 68, "explore"));
        items.save(item(SceneType.OUTFIT, "通勤快干衬衫", "高效率、免熨烫，工作首选。", 60, "structured"));
        items.save(item(SceneType.OUTFIT, "派对亮片装", "闪耀全场，适合高能量社交场景。", 71, "social", "explore"));
        items.save(item(SceneType.OUTFIT, "中性宽松风", "不被性别定义，舒适自在。", 63, "gentle", "explore"));
        items.save(item(SceneType.OUTFIT, "定制西装", "精准合身，重视品质与秩序。", 64, "structured"));
        items.save(item(SceneType.OUTFIT, "民族风刺绣", "独特文化表达，适合高开放性。", 69, "explore", "gentle"));
        items.save(item(SceneType.OUTFIT, "骑行装备", "功能性优先，计划明确。", 61, "structured"));
        items.save(item(SceneType.OUTFIT, "孕妇舒适装", "安全舒适第一，适合特殊时期。", 59, "gentle"));
        items.save(item(SceneType.OUTFIT, "情侣搭配款", "社交表达、关系展示。", 67, "social", "gentle"));
        items.save(item(SceneType.OUTFIT, "面试正装", "目的明确、规矩第一。", 60, "structured"));
        items.save(item(SceneType.OUTFIT, "睡衣外穿风", "打破常规、前卫松弛。", 70, "explore"));
        items.save(item(SceneType.OUTFIT, "二手古着", "环保、独特、有故事感。", 66, "explore", "gentle"));
        items.save(item(SceneType.OUTFIT, "科技面料衣", "现代感、实用性，适合重视效率者。", 63, "structured", "explore"));
        // CAREER: 30 items
        items.save(item(SceneType.CAREER, "创意探索路径", "好奇心转项目作品，小实验积累方向感。", 68, "explore"));
        items.save(item(SceneType.CAREER, "计划执行路径", "阶段目标、复盘指标、稳定输出。", 70, "structured"));
        items.save(item(SceneType.CAREER, "协作沟通路径", "团队任务中发挥关系协调与表达优势。", 64, "social", "gentle"));
        items.save(item(SceneType.CAREER, "深度专注路径", "减少干扰、围绕主题持续积累。", 66, "structured", "gentle"));
        items.save(item(SceneType.CAREER, "创业启动路径", "高风险高回报，适合探索精神。", 72, "explore", "structured"));
        items.save(item(SceneType.CAREER, "自由职业路径", "自主掌控节奏，适合独立型人格。", 67, "explore", "gentle"));
        items.save(item(SceneType.CAREER, "大厂晋升路径", "明确职级体系，稳定可预期。", 65, "structured"));
        items.save(item(SceneType.CAREER, "学术研究路径", "深度钻研，适合高尽责性。", 66, "structured", "explore"));
        items.save(item(SceneType.CAREER, "跨界转型路径", "拥抱不确定性，适合高开放性。", 69, "explore", "social"));
        items.save(item(SceneType.CAREER, "斜槓多重职业", "多元发展，社交与探索并重。", 68, "explore", "social"));
        items.save(item(SceneType.CAREER, "公务员路径", "稳定、规律，适合规避风险偏好。", 62, "structured", "gentle"));
        items.save(item(SceneType.CAREER, "教育培训路径", "助人成长，适合高宜人性。", 64, "gentle", "social"));
        items.save(item(SceneType.CAREER, "技术专家路径", "深耕单一领域，不需过多社交。", 67, "structured", "gentle"));
        items.save(item(SceneType.CAREER, "销售达人路径", "社交驱动、目标明确、高回报。", 66, "social", "structured"));
        items.save(item(SceneType.CAREER, "设计创意路径", "美感与技术结合，适合探索型。", 69, "explore"));
        items.save(item(SceneType.CAREER, "数据分析路径", "逻辑清晰、结果导向。", 65, "structured", "explore"));
        items.save(item(SceneType.CAREER, "医护职业路径", "照顾他人、稳定且有使命感。", 63, "gentle", "structured"));
        items.save(item(SceneType.CAREER, "媒体传播路径", "连接人与信息，社交能量充沛。", 67, "social", "explore"));
        items.save(item(SceneType.CAREER, "金融投资路径", "风险与回报分析，适合冷静决策者。", 66, "structured"));
        items.save(item(SceneType.CAREER, "心理咨询路径", "深度倾听、助人成长。", 64, "gentle", "explore"));
        items.save(item(SceneType.CAREER, "远程办公路径", "自驱力强，适合低社交需求。", 63, "gentle", "structured"));
        items.save(item(SceneType.CAREER, "跨境电商路径", "全球化视野，适合高开放性。", 68, "explore", "social"));
        items.save(item(SceneType.CAREER, "餐饮创业路径", "用美食连接人群。", 65, "social", "explore"));
        items.save(item(SceneType.CAREER, "手艺人路径", "专注技艺、慢工出细活。", 62, "gentle", "structured"));
        items.save(item(SceneType.CAREER, "游戏开发路径", "创意与技术结合，适合探索型。", 70, "explore", "structured"));
        items.save(item(SceneType.CAREER, "法律服务路径", "规则明确、逻辑严密。", 64, "structured"));
        items.save(item(SceneType.CAREER, "非营利组织路径", "使命驱动，适合高宜人性。", 62, "gentle", "social"));
        items.save(item(SceneType.CAREER, "旅游行业路径", "边工作边探索世界。", 68, "explore", "social"));
        items.save(item(SceneType.CAREER, "艺术创作路径", "自由表达、不拘一格。", 71, "explore", "gentle"));
        items.save(item(SceneType.CAREER, "稳定编制路径", "铁饭碗、低风险、可预期。", 61, "structured", "gentle"));
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
            results.save(result(userA, TestType.PERSONALITY, 82, 66, 74, 68, 38, 75, 80, 72, 54, 78));
            results.save(result(userA, TestType.FOOD, 78, 52, 72, 61, 35, 88, 82, 65, 50, 70));
            results.save(result(userA, TestType.TRAVEL, 86, 58, 70, 62, 34, 70, 65, 90, 48, 72));
            results.save(result(userA, TestType.SOCIAL, 76, 64, 82, 72, 36, 68, 85, 72, 55, 88));
        }
        if (results.findByUserOrderByCreatedAtDesc(userB).isEmpty()) {
            results.save(result(userB, TestType.PERSONALITY, 54, 84, 42, 76, 48, 45, 42, 38, 86, 35));
            results.save(result(userB, TestType.FOOD, 48, 80, 38, 72, 46, 52, 48, 42, 82, 40));
            results.save(result(userB, TestType.TRAVEL, 52, 82, 40, 74, 44, 48, 40, 55, 88, 38));
            results.save(result(userB, TestType.SOCIAL, 58, 78, 46, 82, 42, 50, 45, 48, 75, 44));
        }
    }

    private TestResult result(UserAccount user, TestType type, int openness, int conscientiousness, int extraversion, int agreeableness, int neuroticism, int foodAdventure, int foodSocial, int travelAdventure, int travelPlanning, int socialEnergy) {
        TestResult result = new TestResult();
        result.setUser(user);
        result.setType(type);
        result.setScores(Map.of(
                "OPENNESS", openness,
                "CONSCIENTIOUSNESS", conscientiousness,
                "EXTRAVERSION", extraversion,
                "AGREEABLENESS", agreeableness,
                "NEUROTICISM", neuroticism,
                "FOOD_ADVENTURE", foodAdventure,
                "FOOD_SOCIAL", foodSocial,
                "TRAVEL_ADVENTURE", travelAdventure,
                "TRAVEL_PLANNING", travelPlanning,
                "SOCIAL_ENERGY", socialEnergy));
        return result;
    }
}
