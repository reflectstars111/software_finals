package com.personality.radar.controller;

import com.personality.radar.ai.dto.RestaurantCandidate;
import com.personality.radar.ai.service.AiRecommendationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ai")
public class AiRecommendationController {

    private final AiRecommendationService service;

    public AiRecommendationController(AiRecommendationService service) {
        this.service = service;
    }

    @GetMapping("/restaurants")
    public List<RestaurantCandidate> recommend(
            @RequestParam double lat,
            @RequestParam double lng
    ) {
        return service.recommend(lat, lng);
    }
}