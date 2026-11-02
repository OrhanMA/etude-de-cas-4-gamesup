package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.dto.RecommendationDto;

public interface RecommendationService {
    RecommendationDto getRecommendations(String userEmail);
}
