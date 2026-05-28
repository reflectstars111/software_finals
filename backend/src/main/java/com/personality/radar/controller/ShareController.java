package com.personality.radar.controller;

import com.personality.radar.common.ApiResponse;
import com.personality.radar.dto.ApiDtos;
import com.personality.radar.service.CurrentUserService;
import com.personality.radar.service.ShareService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shares")
public class ShareController {
    private final CurrentUserService currentUser;
    private final ShareService shareService;

    public ShareController(CurrentUserService currentUser, ShareService shareService) {
        this.currentUser = currentUser;
        this.shareService = shareService;
    }

    @PostMapping("/report")
    public ApiResponse<ApiDtos.ShareResponse> create() {
        return ApiResponse.ok(shareService.create(currentUser.requireUser()));
    }

    @GetMapping("/{token}")
    public ApiResponse<ApiDtos.ReportResponse> get(@PathVariable String token) {
        return ApiResponse.ok(shareService.get(token));
    }
}

