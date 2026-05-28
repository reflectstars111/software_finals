package com.personality.radar.controller;

import com.personality.radar.common.ApiResponse;
import com.personality.radar.dto.ApiDtos;
import com.personality.radar.service.CurrentUserService;
import com.personality.radar.service.ReportService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/history")
    public ApiResponse<List<ApiDtos.ReportSnapshotResponse>> history() {
        return ApiResponse.ok(reportService.history(currentUser.requireUser()));
    }

    @GetMapping("/{id}")
    public ApiResponse<ApiDtos.ReportSnapshotResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(reportService.get(currentUser.requireUser(), id));
    }
}
