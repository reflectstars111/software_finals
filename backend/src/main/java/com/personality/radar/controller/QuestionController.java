package com.personality.radar.controller;

import com.personality.radar.common.ApiResponse;
import com.personality.radar.dto.ApiDtos;
import com.personality.radar.service.TestService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {
    private final TestService testService;

    public QuestionController(TestService testService) {
        this.testService = testService;
    }

    @GetMapping
    public ApiResponse<List<ApiDtos.QuestionResponse>> list(@RequestParam String type) {
        return ApiResponse.ok(testService.listQuestions(type));
    }
}

