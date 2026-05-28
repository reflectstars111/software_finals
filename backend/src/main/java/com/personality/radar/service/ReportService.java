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
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {
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
        TestResult result = results.findFirstByUserAndTypeOrderByCreatedAtDesc(user, TestType.PERSONALITY)
                .orElseThrow(() -> new BusinessException(400, "请先完成基础性格测试"));
        ApiDtos.ReportResponse report = buildReport(user, result);
        ReportSnapshot snapshot = new ReportSnapshot();
        snapshot.setOwner(user);
        snapshot.setSourceResult(result);
        snapshot.setSummary(report.interpretations().isEmpty() ? "性格雷达报告" : report.interpretations().get(0));
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

    private ApiDtos.ReportResponse buildReport(UserAccount user, TestResult result) {
        Map<String, Integer> scores = result.getScores();
        List<ApiDtos.RadarIndicator> indicators = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        List<String> interpretations = new ArrayList<>();
        for (PersonalityDimension dimension : PersonalityDimension.values()) {
            int score = scores.getOrDefault(dimension.name(), 50);
            indicators.add(new ApiDtos.RadarIndicator(dimension.label(), 100));
            values.add(score);
            interpretations.add(dimension.label() + "：" + score + " 分，" + interpretation(dimension, score));
        }
        List<String> suggestions = List.of(
                "饮食：选择与精力节奏匹配的餐饮方案，保持稳定且轻松的日常体验。",
                "旅行：结合开放性与尽责性分数，平衡探索感和计划性。",
                "穿搭：用颜色、材质和场景匹配表达个人性格特征。",
                "生涯：将优势维度转化为学习、协作和职业规划策略。");
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
        String level = score >= 70 ? "表现突出" : score >= 45 ? "处于均衡区间" : "相对内敛";
        return switch (dimension) {
            case OPENNESS -> level + "，适合尝试新鲜体验与创意任务。";
            case CONSCIENTIOUSNESS -> level + "，可据此安排计划、目标和执行节奏。";
            case EXTRAVERSION -> level + "，影响社交、表达和活动偏好。";
            case AGREEABLENESS -> level + "，影响协作、共情和关系维护方式。";
            case NEUROTICISM -> level + "，建议关注压力管理与情绪恢复。";
        };
    }
}
