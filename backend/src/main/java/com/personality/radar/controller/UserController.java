package com.personality.radar.controller;

import com.personality.radar.common.ApiResponse;
import com.personality.radar.domain.UserAccount;
import com.personality.radar.dto.ApiDtos;
import com.personality.radar.service.CurrentUserService;
import com.personality.radar.service.DtoMapper;
import com.personality.radar.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final CurrentUserService currentUser;
    private final UserService userService;

    public UserController(CurrentUserService currentUser, UserService userService) {
        this.currentUser = currentUser;
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<ApiDtos.UserProfileResponse> me() {
        return ApiResponse.ok(DtoMapper.user(currentUser.requireUser()));
    }

    @PutMapping("/me")
    public ApiResponse<ApiDtos.UserProfileResponse> update(@Valid @RequestBody ApiDtos.UpdateUserRequest request) {
        UserAccount user = currentUser.requireUser();
        return ApiResponse.ok(userService.update(user, request));
    }
}

