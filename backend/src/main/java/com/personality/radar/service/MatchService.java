package com.personality.radar.service;

import com.personality.radar.common.BusinessException;
import com.personality.radar.domain.CompatibilityReport;
import com.personality.radar.domain.PersonalityDimension;
import com.personality.radar.domain.TestResult;
import com.personality.radar.domain.TestType;
import com.personality.radar.domain.UserAccount;
import com.personality.radar.dto.ApiDtos;
import com.personality.radar.repository.CompatibilityReportRepository;
import com.personality.radar.repository.TestResultRepository;
import com.personality.radar.repository.UserRepository;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchService {
    private final UserRepository users;
    private final TestResultRepository results;
    private final CompatibilityReportRepository reports;

    public MatchService(UserRepository users, TestResultRepository results, CompatibilityReportRepository reports) {
        this.users = users;
        this.results = results;
        this.reports = reports;
    }

    @Transactional
    public ApiDtos.MatchResponse create(UserAccount owner, ApiDtos.MatchRequest request) {
        UserAccount target = users.findByPhone(request.friendPhone())
                .orElseThrow(() -> new BusinessException(404, "好友ID不存在"));
        if (target.getId().equals(owner.getId())) {
            throw new BusinessException(400, "不能和自己生成适配报告");
        }
        TestResult left = results.findFirstByUserAndTypeOrderByCreatedAtDesc(owner, TestType.PERSONALITY)
                .orElseThrow(() -> new BusinessException(400, "请先完成自己的基础性格测试"));
        TestResult right = results.findFirstByUserAndTypeOrderByCreatedAtDesc(target, TestType.PERSONALITY)
                .orElseThrow(() -> new BusinessException(400, "好友尚未完成基础性格测试"));
        double score = MatchEngine.compatibilityScore(left.getScores(), right.getScores());
        CompatibilityReport report = new CompatibilityReport();
        report.setOwner(owner);
        report.setTarget(target);
        report.setScore(score);
        report.setSummary(score >= 80 ? "高度契合，适合共同推进计划与探索新体验。" : score >= 60 ? "整体匹配，可通过沟通管理差异。" : "差异明显，建议提前约定边界与节奏。");
        report.setAdvantages(String.join(";", strongest(left.getScores(), right.getScores(), true)));
        report.setWarnings(String.join(";", strongest(left.getScores(), right.getScores(), false)));
        report.setAdvice("先确认共同目标;对高差异维度设置沟通规则;用一次短反馈替代长时间猜测");
        reports.save(report);
        return toResponse(report);
    }

    public ApiDtos.MatchResponse get(UserAccount owner, Long id) {
        return reports.findByIdAndOwner(id, owner).map(this::toResponse)
                .orElseThrow(() -> new BusinessException(404, "适配报告不存在"));
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.MatchResponse> list(UserAccount owner) {
        return reports.findByOwnerOrderByCreatedAtDesc(owner).stream()
                .map(this::toResponse)
                .toList();
    }

    private ApiDtos.MatchResponse toResponse(CompatibilityReport report) {
        return new ApiDtos.MatchResponse(
                report.getId(),
                DtoMapper.user(report.getOwner()),
                DtoMapper.user(report.getTarget()),
                report.getScore(),
                report.getSummary(),
                split(report.getAdvantages()),
                split(report.getWarnings()),
                split(report.getAdvice()),
                report.getCreatedAt());
    }

    private List<String> split(String value) {
        return Arrays.stream(value.split(";")).filter(s -> !s.isBlank()).toList();
    }

    private List<String> strongest(Map<String, Integer> left, Map<String, Integer> right, boolean similar) {
        return Arrays.stream(PersonalityDimension.values())
                .sorted(Comparator.comparingInt(d -> Math.abs(left.getOrDefault(d.name(), 50) - right.getOrDefault(d.name(), 50))))
                .skip(similar ? 0 : Math.max(0, PersonalityDimension.values().length - 2))
                .limit(2)
                .map(d -> similar
                        ? d.label() + "接近，互动节奏更容易达成共识"
                        : d.label() + "差异较大，建议提前沟通期望")
                .toList();
    }
}
