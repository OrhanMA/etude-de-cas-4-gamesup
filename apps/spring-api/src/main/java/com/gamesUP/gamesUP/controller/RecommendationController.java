package com.gamesUP.gamesUP.controller;

import com.gamesUP.gamesUP.dto.RecommendationDto;
import com.gamesUP.gamesUP.service.RecommendationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public RecommendationDto getRecommendations(@AuthenticationPrincipal Jwt jwt) {
        return recommendationService.getRecommendations(jwt.getSubject());
    }
}
