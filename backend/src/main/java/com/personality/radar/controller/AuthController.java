package com.personality.radar.controller;

import com.personality.radar.common.ApiResponse;
import com.personality.radar.dto.ApiDtos;
import com.personality.radar.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<ApiDtos.AuthResponse> register(@Valid @RequestBody ApiDtos.RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<ApiDtos.AuthResponse> login(@Valid @RequestBody ApiDtos.LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }
}

