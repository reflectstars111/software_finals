package com.personality.radar.controller;

import com.personality.radar.common.ApiResponse;
import com.personality.radar.dto.ApiDtos;
import com.personality.radar.service.CurrentUserService;
import com.personality.radar.service.MatchService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matches")
public class MatchController {
    private final CurrentUserService currentUser;
    private final MatchService matchService;

    public MatchController(CurrentUserService currentUser, MatchService matchService) {
        this.currentUser = currentUser;
        this.matchService = matchService;
    }

    @PostMapping
    public ApiResponse<ApiDtos.MatchResponse> create(@Valid @RequestBody ApiDtos.MatchRequest request) {
        return ApiResponse.ok(matchService.create(currentUser.requireUser(), request));
    }

    @GetMapping("/{id}")
    public ApiResponse<ApiDtos.MatchResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(matchService.get(currentUser.requireUser(), id));
    }
}

