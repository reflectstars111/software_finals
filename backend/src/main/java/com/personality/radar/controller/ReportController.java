package com.personality.radar.controller;

import com.personality.radar.common.ApiResponse;
import com.personality.radar.dto.ApiDtos;
import com.personality.radar.service.CurrentUserService;
import com.personality.radar.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final CurrentUserService currentUser;
    private final ReportService reportService;

    public ReportController(CurrentUserService currentUser, ReportService reportService) {
        this.currentUser = currentUser;
        this.reportService = reportService;
    }

    @GetMapping("/me")
    public ApiResponse<ApiDtos.ReportResponse> me() {
        return ApiResponse.ok(reportService.reportFor(currentUser.requireUser()));
    }
}

