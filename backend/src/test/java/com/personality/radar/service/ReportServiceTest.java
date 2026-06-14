package com.personality.radar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.personality.radar.domain.ReportSnapshot;
import com.personality.radar.domain.Role;
import com.personality.radar.domain.TestResult;
import com.personality.radar.domain.TestType;
import com.personality.radar.domain.UserAccount;
import com.personality.radar.dto.ApiDtos;
import com.personality.radar.repository.ReportSnapshotRepository;
import com.personality.radar.repository.TestResultRepository;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ReportServiceTest {
    @Test
    void buildsReadableSampleReportCopyFromLatestPersonalityResult() {
        UserAccount user = new UserAccount();
        user.setPhone("13900000001");
        user.setDisplayName("晨间探索者");
        user.setPasswordHash("hash");
        user.setRole(Role.USER);

        TestResult result = new TestResult();
        result.setUser(user);
        result.setType(TestType.PERSONALITY);
        result.setScores(Map.of(
                "OPENNESS", 82,
                "CONSCIENTIOUSNESS", 66,
                "EXTRAVERSION", 74,
                "AGREEABLENESS", 68,
                "NEUROTICISM", 38));

        TestResultRepository results = org.mockito.Mockito.mock(TestResultRepository.class);
        ReportSnapshotRepository snapshots = org.mockito.Mockito.mock(ReportSnapshotRepository.class);
        when(results.findFirstByUserAndTypeOrderByCreatedAtDesc(user, TestType.PERSONALITY))
                .thenReturn(Optional.of(result));
        when(snapshots.save(any(ReportSnapshot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        ApiDtos.ReportResponse report = new ReportService(results, snapshots, mapper).reportFor(user);

        assertThat(report.indicators()).extracting(ApiDtos.RadarIndicator::name)
                .containsExactly("开放性", "尽责性", "外向性", "宜人性", "情绪稳定性");
        assertThat(report.interpretations())
                .anySatisfy(line -> assertThat(line).contains("开放性：82 分，你更容易被新体验、创意和未知路线吸引"))
                .anySatisfy(line -> assertThat(line).contains("情绪稳定性：62 分，你面对变化更稳定"));
        assertThat(report.suggestions())
                .contains(
                        "这份报告基于最近一次性格、饮食、旅行和社交测试生成，用来帮助你理解偏好、节奏和适配方式。",
                        "给自己安排固定的“探索窗口”，让新鲜感变成可持续的行动。");
        assertThat(report.interpretations()).allSatisfy(line -> assertThat(line).doesNotContain("锛", "鍦", "�"));
        assertThat(report.suggestions()).allSatisfy(line -> assertThat(line).doesNotContain("锛", "鍦", "�"));
    }
}
