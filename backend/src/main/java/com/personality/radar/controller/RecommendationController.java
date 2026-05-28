package com.personality.radar.controller;

import com.personality.radar.common.ApiResponse;
import com.personality.radar.dto.ApiDtos;
import com.personality.radar.service.CurrentUserService;
import com.personality.radar.service.RecommendationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    private final CurrentUserService currentUser;
    private final RecommendationService recommendationService;

    public RecommendationController(CurrentUserService currentUser, RecommendationService recommendationService) {
        this.currentUser = currentUser;
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public ApiResponse<List<ApiDtos.RecommendationResponse>> list(@RequestParam String scene) {
        return ApiResponse.ok(recommendationService.recommend(currentUser.requireUser(), scene));
    }

    @PostMapping("/{id}/feedback")
    public ApiResponse<Void> feedback(@PathVariable Long id, @Valid @RequestBody ApiDtos.FeedbackRequest request) {
        recommendationService.feedback(currentUser.requireUser(), id, request);
        return ApiResponse.ok();
    }
}

