package com.personality.radar.service;

import com.personality.radar.common.BusinessException;
import com.personality.radar.domain.CompatibilityReport;
import com.personality.radar.domain.MatchInvite;
import com.personality.radar.domain.PersonalityDimension;
import com.personality.radar.domain.TestResult;
import com.personality.radar.domain.TestType;
import com.personality.radar.domain.UserAccount;
import com.personality.radar.dto.ApiDtos;
import com.personality.radar.repository.CompatibilityReportRepository;
import com.personality.radar.repository.MatchInviteRepository;
import com.personality.radar.repository.TestResultRepository;
import com.personality.radar.repository.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchService {
    private static final int INVITE_EXPIRY_DAYS = 90;

    private final UserRepository users;
    private final TestResultRepository results;
    private final CompatibilityReportRepository reports;
    private final MatchInviteRepository invites;

    public MatchService(UserRepository users, TestResultRepository results,
                        CompatibilityReportRepository reports, MatchInviteRepository invites) {
        this.users = users;
        this.results = results;
        this.reports = reports;
        this.invites = invites;
    }

    @Transactional
    public ApiDtos.MatchInviteResponse createInvite(UserAccount owner) {
        if (results.findFirstByUserAndTypeOrderByCreatedAtDesc(owner, TestType.PERSONALITY).isEmpty()) {
            throw new BusinessException(400, "请先完成基础性格测试，再生成匹配邀请码。");
        }
        // Revoke existing active invites for this user
        invites.findByOwnerAndStatus(owner, MatchInvite.MatchInviteStatus.ACTIVE)
                .ifPresent(invite -> {
                    invite.setStatus(MatchInvite.MatchInviteStatus.REVOKED);
                    invites.save(invite);
                });
        MatchInvite invite = new MatchInvite();
        invite.setCode(MatchInvite.generateCode());
        invite.setOwner(owner);
        invite.setStatus(MatchInvite.MatchInviteStatus.ACTIVE);
        invite.setCreatedAt(Instant.now());
        invite.setExpiresAt(Instant.now().plus(INVITE_EXPIRY_DAYS, ChronoUnit.DAYS));
        invites.save(invite);
        return new ApiDtos.MatchInviteResponse(invite.getCode(), invite.getCreatedAt(),
                invite.getStatus().name(), invite.getExpiresAt());
    }

    @Transactional
    public ApiDtos.MatchResponse createByInvite(UserAccount user, ApiDtos.MatchByInviteRequest request) {
        MatchInvite invite = invites.findByCode(request.inviteCode().trim().toUpperCase())
                .orElseThrow(() -> new BusinessException(404, "邀请码错误，请确认后重新输入。"));
        if (invite.getStatus() != MatchInvite.MatchInviteStatus.ACTIVE) {
            throw new BusinessException(400, "该邀请码已失效（已使用或已撤销）。");
        }
        if (invite.getExpiresAt().isBefore(Instant.now())) {
            invite.setStatus(MatchInvite.MatchInviteStatus.REVOKED);
            invites.save(invite);
            throw new BusinessException(400, "该邀请码已过期。");
        }
        UserAccount inviter = invite.getOwner();
        if (inviter.getId().equals(user.getId())) {
            throw new BusinessException(400, "不能和自己生成适配报告");
        }
        TestResult left = results.findFirstByUserAndTypeOrderByCreatedAtDesc(user, TestType.PERSONALITY)
                .orElseThrow(() -> new BusinessException(400, "请先完成自己的基础性格测试"));
        TestResult right = results.findFirstByUserAndTypeOrderByCreatedAtDesc(inviter, TestType.PERSONALITY)
                .orElseThrow(() -> new BusinessException(400, "对方尚未完成基础性格测试"));
        double score = MatchEngine.compatibilityScore(left.getScores(), right.getScores());
        CompatibilityReport report = buildReport(user, inviter, left.getScores(), right.getScores(), score);
        reports.save(report);
        invite.setStatus(MatchInvite.MatchInviteStatus.USED);
        invites.save(invite);
        return toResponse(report, left.getScores(), right.getScores());
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
        CompatibilityReport report = buildReport(owner, target, left.getScores(), right.getScores(), score);
        reports.save(report);
        return toResponse(report, left.getScores(), right.getScores());
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.MatchInviteResponse> listInvites(UserAccount owner) {
        return invites.findByOwnerOrderByCreatedAtDesc(owner).stream()
                .map(invite -> new ApiDtos.MatchInviteResponse(
                        invite.getCode(), invite.getCreatedAt(),
                        invite.getStatus().name(), invite.getExpiresAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ApiDtos.MatchResponse get(UserAccount owner, Long id) {
        CompatibilityReport report = reports.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new BusinessException(404, "适配报告不存在"));
        Map<String, Integer> ownerScores = latestPersonalityScores(owner);
        Map<String, Integer> targetScores = latestPersonalityScores(report.getTarget());
        return toResponse(report, ownerScores, targetScores);
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.MatchResponse> list(UserAccount owner) {
        List<CompatibilityReport> reportList = reports.findByOwnerOrderByCreatedAtDesc(owner);
        if (reportList.isEmpty()) return List.of();
        Map<String, Integer> ownerScores = latestPersonalityScores(owner);
        Map<Long, Map<String, Integer>> targetScoreCache = new java.util.HashMap<>();
        return reportList.stream()
                .map(report -> {
                    Map<String, Integer> targetScores = targetScoreCache.computeIfAbsent(
                            report.getTarget().getId(),
                            id -> latestPersonalityScores(report.getTarget()));
                    return toResponse(report, ownerScores, targetScores);
                })
                .toList();
    }

    private Map<String, Integer> latestPersonalityScores(UserAccount user) {
        return results.findFirstByUserAndTypeOrderByCreatedAtDesc(user, TestType.PERSONALITY)
                .<Map<String, Integer>>map(r -> new java.util.HashMap<>(r.getScores()))
                .orElseGet(java.util.HashMap::new);
    }

    private CompatibilityReport buildReport(UserAccount owner, UserAccount target,
                                            Map<String, Integer> leftScores,
                                            Map<String, Integer> rightScores, double score) {
        CompatibilityReport report = new CompatibilityReport();
        report.setOwner(owner);
        report.setTarget(target);
        report.setScore(score);
        report.setSummary(score >= 80 ? "高度契合，适合共同推进计划与探索新体验。"
                : score >= 60 ? "整体匹配，可通过沟通管理差异。"
                : "差异明显，建议提前约定边界与节奏。");
        report.setAdvantages(String.join(";", strongest(leftScores, rightScores, true)));
        report.setWarnings(String.join(";", strongest(leftScores, rightScores, false)));
        report.setAdvice("先确认共同目标;对高差异维度设置沟通规则;用一次短反馈替代长时间猜测");
        return report;
    }

    private ApiDtos.MatchResponse toResponse(CompatibilityReport report,
                                              Map<String, Integer> ownerScores,
                                              Map<String, Integer> targetScores) {
        return new ApiDtos.MatchResponse(
                report.getId(),
                DtoMapper.user(report.getOwner()),
                DtoMapper.user(report.getTarget()),
                report.getScore(),
                report.getSummary(),
                split(report.getAdvantages()),
                split(report.getWarnings()),
                split(report.getAdvice()),
                ownerScores,
                targetScores,
                report.getCreatedAt());
    }

    private List<String> split(String value) {
        return Arrays.stream(value.split(";")).filter(s -> !s.isBlank()).toList();
    }

    private List<String> strongest(Map<String, Integer> left, Map<String, Integer> right, boolean similar) {
        return Arrays.stream(PersonalityDimension.values())
                .sorted(Comparator.comparingInt(d ->
                        Math.abs(left.getOrDefault(d.name(), 50) - right.getOrDefault(d.name(), 50))))
                .skip(similar ? 0 : Math.max(0, PersonalityDimension.values().length - 2))
                .limit(2)
                .map(d -> similar
                        ? d.label() + "接近，互动节奏更容易达成共识"
                        : d.label() + "差异较大，建议提前沟通期望")
                .toList();
    }
}
