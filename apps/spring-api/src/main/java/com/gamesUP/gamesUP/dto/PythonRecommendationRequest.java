package com.gamesUP.gamesUP.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PythonRecommendationRequest(
        @JsonProperty("user_id") Long userId,
        List<PythonUserInteraction> interactions,
        List<PythonCatalogGame> catalog,
        int limit
) {}
