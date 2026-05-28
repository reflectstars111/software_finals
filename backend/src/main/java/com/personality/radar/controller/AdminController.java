package com.personality.radar.controller;

import com.personality.radar.common.ApiResponse;
import com.personality.radar.dto.ApiDtos;
import com.personality.radar.service.AdminService;
import com.personality.radar.service.CurrentUserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final CurrentUserService currentUser;
    private final AdminService adminService;

    public AdminController(CurrentUserService currentUser, AdminService adminService) {
        this.currentUser = currentUser;
        this.adminService = adminService;
    }

    @GetMapping("/questions")
    public ApiResponse<List<ApiDtos.QuestionResponse>> questions() {
        return ApiResponse.ok(adminService.allQuestions());
    }

    @PostMapping("/questions")
    public ApiResponse<ApiDtos.QuestionResponse> createQuestion(@Valid @RequestBody ApiDtos.AdminQuestionRequest request) {
        return ApiResponse.ok(adminService.createQuestion(currentUser.requireUser(), request));
    }

    @PutMapping("/questions/{id}")
    public ApiResponse<ApiDtos.QuestionResponse> updateQuestion(@PathVariable Long id, @Valid @RequestBody ApiDtos.AdminQuestionRequest request) {
        return ApiResponse.ok(adminService.updateQuestion(currentUser.requireUser(), id, request));
    }

    @DeleteMapping("/questions/{id}")
    public ApiResponse<Void> deleteQuestion(@PathVariable Long id) {
        adminService.deleteQuestion(currentUser.requireUser(), id);
        return ApiResponse.ok();
    }

    @GetMapping("/recommendation-items")
    public ApiResponse<List<ApiDtos.RecommendationResponse>> recommendationItems() {
        return ApiResponse.ok(adminService.allRecommendationItems());
    }

    @PostMapping("/recommendation-items")
    public ApiResponse<ApiDtos.RecommendationResponse> createRecommendation(@Valid @RequestBody ApiDtos.RecommendationItemRequest request) {
        return ApiResponse.ok(adminService.createRecommendationItem(currentUser.requireUser(), request));
    }

    @PutMapping("/recommendation-items/{id}")
    public ApiResponse<ApiDtos.RecommendationResponse> updateRecommendation(@PathVariable Long id, @Valid @RequestBody ApiDtos.RecommendationItemRequest request) {
        return ApiResponse.ok(adminService.updateRecommendationItem(currentUser.requireUser(), id, request));
    }

    @DeleteMapping("/recommendation-items/{id}")
    public ApiResponse<Void> deleteRecommendation(@PathVariable Long id) {
        adminService.deleteRecommendationItem(currentUser.requireUser(), id);
        return ApiResponse.ok();
    }

    @GetMapping("/feedback")
    public ApiResponse<List<ApiDtos.AdminFeedbackResponse>> feedback() {
        return ApiResponse.ok(adminService.feedback());
    }

    @GetMapping("/logs")
    public ApiResponse<List<ApiDtos.AdminLogResponse>> logs() {
        return ApiResponse.ok(adminService.logs());
    }

    @GetMapping("/stats")
    public ApiResponse<ApiDtos.AdminStatsResponse> stats() {
        return ApiResponse.ok(adminService.stats());
    }
}

