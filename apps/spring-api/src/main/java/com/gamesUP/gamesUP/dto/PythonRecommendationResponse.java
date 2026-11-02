package com.gamesUP.gamesUP.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PythonRecommendationResponse(
        @JsonProperty("user_id") Long userId,
        String strategy,
        List<PythonGameRecommendation> recommendations
) {}
