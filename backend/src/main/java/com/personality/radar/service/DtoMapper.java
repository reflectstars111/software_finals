package com.personality.radar.service;

import com.personality.radar.domain.Question;
import com.personality.radar.domain.QuestionOption;
import com.personality.radar.domain.RecommendationItem;
import com.personality.radar.domain.TestResult;
import com.personality.radar.domain.UserAccount;
import com.personality.radar.dto.ApiDtos;
import java.util.List;

public final class DtoMapper {
    private DtoMapper() {
    }

    public static ApiDtos.UserProfileResponse user(UserAccount user) {
        return new ApiDtos.UserProfileResponse(
                user.getId(),
                user.getPhone(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getRole().name());
    }

    public static ApiDtos.QuestionResponse question(Question question) {
        return new ApiDtos.QuestionResponse(
                question.getId(),
                question.getType().name().toLowerCase(),
                question.getContent(),
                question.isActive(),
                question.getOptions().stream().map(DtoMapper::option).toList());
    }

    public static ApiDtos.OptionResponse option(QuestionOption option) {
        return new ApiDtos.OptionResponse(
                option.getId(),
                option.getLabel(),
                option.getContent(),
                option.getWeights());
    }

    public static ApiDtos.TestResultResponse testResult(TestResult result) {
        return new ApiDtos.TestResultResponse(
                result.getId(),
                result.getType().name().toLowerCase(),
                result.getScores(),
                result.getCreatedAt());
    }

    public static ApiDtos.RecommendationResponse recommendation(RecommendationItem item, int score) {
        return new ApiDtos.RecommendationResponse(
                item.getId(),
                item.getScene().name().toLowerCase(),
                item.getTitle(),
                item.getDescription(),
                List.copyOf(item.getTags()),
                score);
    }
}

