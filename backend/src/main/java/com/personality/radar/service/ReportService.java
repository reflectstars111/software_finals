package com.personality.radar.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personality.radar.common.BusinessException;
import com.personality.radar.domain.PersonalityDimension;
import com.personality.radar.domain.ReportSnapshot;
import com.personality.radar.domain.TestResult;
import com.personality.radar.domain.TestType;
import com.personality.radar.domain.UserAccount;
import com.personality.radar.dto.ApiDtos;
import com.personality.radar.repository.ReportSnapshotRepository;
import com.personality.radar.repository.TestResultRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {
    private static final String REPORT_INTRO = "这份报告基于最近一次性格、饮食、旅行和社交测试生成，用来帮助你理解偏好、节奏和适配方式。";

    private final TestResultRepository results;
    private final ReportSnapshotRepository snapshots;
    private final ObjectMapper mapper;

    public ReportService(TestResultRepository results, ReportSnapshotRepository snapshots, ObjectMapper mapper) {
        this.results = results;
        this.snapshots = snapshots;
        this.mapper = mapper;
    }

    @Transactional
    public ApiDtos.ReportResponse reportFor(UserAccount user) {
        // Aggregate latest results from all four test types
        Map<String, Integer> mergedScores = new java.util.HashMap<>();
        for (PersonalityDimension dim : PersonalityDimension.values()) {
            mergedScores.put(dim.name(), 50);
        }
        for (TestType type : TestType.values()) {
            results.findFirstByUserAndTypeOrderByCreatedAtDesc(user, type)
                    .ifPresent(r -> r.getScores().forEach((dim, score) ->
                            mergedScores.merge(dim, score, (a, b) -> Math.max(a, b))));
        }
        TestResult primary = results.findFirstByUserAndTypeOrderByCreatedAtDesc(user, TestType.PERSONALITY)
                .orElseThrow(() -> new BusinessException(400, "请先完成基础性格测试"));
        ApiDtos.ReportResponse report = buildReport(user, mergedScores);
        ReportSnapshot snapshot = new ReportSnapshot();
        snapshot.setOwner(user);
        snapshot.setSourceResult(primary);
        snapshot.setSummary(report.interpretations().isEmpty() ? "多维性格画像" : report.interpretations().get(0));
        try {
            snapshot.setReportJson(mapper.writeValueAsString(report));
        } catch (JsonProcessingException ex) {
            throw new BusinessException(500, "报告保存失败");
        }
        snapshots.save(snapshot);
        return report;
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.ReportSnapshotResponse> history(UserAccount user) {
        return snapshots.findByOwnerOrderByCreatedAtDesc(user).stream()
                .map(this::snapshotResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ApiDtos.ReportSnapshotResponse get(UserAccount user, Long id) {
        return snapshots.findByIdAndOwner(id, user).map(this::snapshotResponse)
                .orElseThrow(() -> new BusinessException(404, "报告不存在"));
    }

    private ApiDtos.ReportResponse buildReport(UserAccount user, Map<String, Integer> scores) {
        List<ApiDtos.RadarIndicator> indicators = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        List<String> interpretations = new ArrayList<>();
        for (PersonalityDimension dimension : PersonalityDimension.values()) {
            int score = scores.getOrDefault(dimension.name(), 50);
            int displayScore = displayScore(dimension, score);
            String label = displayLabel(dimension);
            indicators.add(new ApiDtos.RadarIndicator(label, 100));
            values.add(displayScore);
            interpretations.add(label + "：" + displayScore + " 分，" + interpretation(dimension, score));
        }
        List<String> suggestions = new ArrayList<>();
        suggestions.add(REPORT_INTRO);
        suggestions.add(prioritySuggestion(scores));
        suggestions.add("饮食：选择与精力节奏匹配的餐饮方案，让日常体验更稳定也更轻松。");
        suggestions.add("旅行：在探索感和计划感之间留出弹性，减少出行中的额外消耗。");
        suggestions.add("社交：根据社交能量和关系协调方式，选择更舒服的互动密度。");
        suggestions.add("匹配：授权后只比较维度差异和相处建议，不展示完整答题内容。");
        return new ApiDtos.ReportResponse(
                DtoMapper.user(user),
                scores,
                indicators,
                values,
                interpretations,
                suggestions,
                Instant.now());
    }

    private ApiDtos.ReportSnapshotResponse snapshotResponse(ReportSnapshot snapshot) {
        try {
            return new ApiDtos.ReportSnapshotResponse(
                    snapshot.getId(),
                    mapper.readValue(snapshot.getReportJson(), ApiDtos.ReportResponse.class),
                    snapshot.getSummary(),
                    snapshot.getCreatedAt());
        } catch (JsonProcessingException ex) {
            throw new BusinessException(500, "报告读取失败");
        }
    }

    private String interpretation(PersonalityDimension dimension, int score) {
        if (score >= 70) {
            return switch (dimension) {
                case OPENNESS -> "你更容易被新体验、创意和未知路线吸引，适合保留探索空间。";
                case CONSCIENTIOUSNESS -> "你重视计划、秩序和可靠交付，适合明确目标和步骤。";
                case EXTRAVERSION -> "你容易从交流、活动和群体互动中获得能量。";
                case AGREEABLENESS -> "你重视关系感受，擅长协调和照顾他人。";
                case NEUROTICISM -> "你对压力和环境变化更敏锐，需要稳定节奏和恢复空间。";
                case FOOD_ADVENTURE -> "你愿意尝试新奇口味和创意餐饮，探索型餐厅更适合你。";
                case FOOD_SOCIAL -> "你享受与朋友分享美食的社交体验，适合聚餐和分享型场景。";
                case TRAVEL_ADVENTURE -> "你向往未知目的地和小众路线，探索型旅行能给你带来满足感。";
                case TRAVEL_PLANNING -> "你重视出行的确定性和可控性，计划明确的旅程让你更安心。";
                case SOCIAL_ENERGY -> "你有充足的社交能量，愿意主动发起聚会并参与群体活动。";
            };
        }
        if (score >= 45) {
            return switch (dimension) {
                case OPENNESS -> "你愿意尝试新东西，但需要明确收益或安全边界。";
                case CONSCIENTIOUSNESS -> "你能在计划和弹性之间切换。";
                case EXTRAVERSION -> "你能社交，也需要独处恢复。";
                case AGREEABLENESS -> "你能平衡自己和他人的需求。";
                case NEUROTICISM -> "你能感知压力，但通常可以自我调节。";
                case FOOD_ADVENTURE -> "你对新口味持开放态度，但也看重熟悉的安全感。";
                case FOOD_SOCIAL -> "你偶尔享受聚餐，但也珍惜独自用餐的安静。";
                case TRAVEL_ADVENTURE -> "你在探索和稳妥之间找平衡，视情况决定冒险程度。";
                case TRAVEL_PLANNING -> "你会做基本规划，但保留一定的弹性空间。";
                case SOCIAL_ENERGY -> "你能适应不同的社交场合，也会给自己留出独处时间。";
            };
        }
        return switch (dimension) {
            case OPENNESS -> "你更偏好熟悉、稳定和可预测的选择。";
            case CONSCIENTIOUSNESS -> "你更依赖即时状态，适合轻量任务和短周期反馈。";
            case EXTRAVERSION -> "你更适合低噪音、深度交流和安静场景。";
            case AGREEABLENESS -> "你更直接表达边界和判断，适合规则清晰的协作。";
            case NEUROTICISM -> "你面对变化更稳定，适合承担不确定性较高的任务。";
            case FOOD_ADVENTURE -> "你更偏好熟悉稳定的口味，固定的老店更让你安心。";
            case FOOD_SOCIAL -> "你更喜欢安静独立的用餐方式，不需要太多社交互动。";
            case TRAVEL_ADVENTURE -> "你更偏爱熟悉的目的地和路线，重复带来的安全感很重要。";
            case TRAVEL_PLANNING -> "你喜欢随性的出行方式，享受即兴和意外的惊喜。";
            case SOCIAL_ENERGY -> "连续社交会让你疲惫，你需要充足的独处时间来恢复能量。";
        };
    }

    private int displayScore(PersonalityDimension dimension, int score) {
        return dimension == PersonalityDimension.NEUROTICISM ? 100 - score : score;
    }

    private String displayLabel(PersonalityDimension dimension) {
        return dimension == PersonalityDimension.NEUROTICISM ? "情绪稳定性" : dimension.label();
    }

    private String prioritySuggestion(Map<String, Integer> scores) {
        PersonalityDimension strongest = scores.entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(entry -> PersonalityDimension.valueOf(entry.getKey()))
                .orElse(PersonalityDimension.OPENNESS);
        return switch (strongest) {
            case OPENNESS -> "给自己安排固定的探索窗口，让新鲜感变成可持续的行动。";
            case CONSCIENTIOUSNESS -> "保留 20% 弹性，避免计划过满带来额外压力。";
            case EXTRAVERSION -> "把重要任务和社交反馈结合，会更容易保持动力。";
            case AGREEABLENESS -> "在照顾他人前先写下自己的底线，减少隐性消耗。";
            case NEUROTICISM -> "优先管理睡眠、环境和节奏，再处理复杂任务。";
            case FOOD_ADVENTURE -> "每周安排一次探索型餐饮，用味觉新鲜感激活生活动力。";
            case FOOD_SOCIAL -> "把聚餐作为社交锚点，定期与朋友分享美食能提升幸福感。";
            case TRAVEL_ADVENTURE -> "定期规划一次探索型短途旅行，用新鲜环境刷新状态。";
            case TRAVEL_PLANNING -> "提前规划旅程节奏，预留足够的缓冲时间减少出行焦虑。";
            case SOCIAL_ENERGY -> "主动管理社交节奏，在高能量时段安排重要互动。";
        };
    }}
