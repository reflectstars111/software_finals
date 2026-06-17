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
    void buildsReadableSampleReportWithAllTenDimensions() {
        UserAccount user = new UserAccount();
        user.setPhone("13900000001");
        user.setDisplayName("晨间探索者");
        user.setPasswordHash("hash");
        user.setRole(Role.USER);

        TestResult personality = new TestResult();
        personality.setUser(user);
        personality.setType(TestType.PERSONALITY);
        personality.setScores(Map.of(
                "OPENNESS", 82, "CONSCIENTIOUSNESS", 66, "EXTRAVERSION", 74,
                "AGREEABLENESS", 68, "NEUROTICISM", 38));

        TestResult food = new TestResult();
        food.setUser(user); food.setType(TestType.FOOD);
        food.setScores(Map.of("OPENNESS", 70, "FOOD_ADVENTURE", 85, "FOOD_SOCIAL", 72));

        TestResult travel = new TestResult();
        travel.setUser(user); travel.setType(TestType.TRAVEL);
        travel.setScores(Map.of("OPENNESS", 75, "TRAVEL_ADVENTURE", 80, "TRAVEL_PLANNING", 55));

        TestResult social = new TestResult();
        social.setUser(user); social.setType(TestType.SOCIAL);
        social.setScores(Map.of("EXTRAVERSION", 80, "SOCIAL_ENERGY", 88));

        TestResultRepository results = org.mockito.Mockito.mock(TestResultRepository.class);
        ReportSnapshotRepository snapshots = org.mockito.Mockito.mock(ReportSnapshotRepository.class);
        when(results.findFirstByUserAndTypeOrderByCreatedAtDesc(user, TestType.PERSONALITY))
                .thenReturn(Optional.of(personality));
        when(results.findFirstByUserAndTypeOrderByCreatedAtDesc(user, TestType.FOOD))
                .thenReturn(Optional.of(food));
        when(results.findFirstByUserAndTypeOrderByCreatedAtDesc(user, TestType.TRAVEL))
                .thenReturn(Optional.of(travel));
        when(results.findFirstByUserAndTypeOrderByCreatedAtDesc(user, TestType.SOCIAL))
                .thenReturn(Optional.of(social));
        when(snapshots.save(any(ReportSnapshot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        ApiDtos.ReportResponse report = new ReportService(results, snapshots, mapper).reportFor(user);

        assertThat(report.indicators()).hasSize(10);
        assertThat(report.indicators()).extracting(ApiDtos.RadarIndicator::name)
                .contains("开放性", "尽责性", "外向性", "宜人性", "情绪稳定性",
                        "饮食探索", "饮食社交", "旅行探索", "旅行计划", "社交能量");
        assertThat(report.interpretations()).hasSize(10);
        assertThat(report.interpretations())
                .anySatisfy(line -> assertThat(line).contains("开放性：82 分，你更容易被新体验、创意和未知路线吸引"))
                .anySatisfy(line -> assertThat(line).contains("饮食探索：85 分"));
        assertThat(report.suggestions())
                .contains("这份报告基于最近一次性格、饮食、旅行和社交测试生成，用来帮助你理解偏好、节奏和适配方式。");
        assertThat(report.scores()).containsEntry("FOOD_ADVENTURE", 85);
        assertThat(report.scores()).containsEntry("SOCIAL_ENERGY", 88);
    }
}
