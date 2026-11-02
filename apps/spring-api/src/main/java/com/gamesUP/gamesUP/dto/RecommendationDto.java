package com.gamesUP.gamesUP.dto;

import java.util.List;

public record RecommendationDto(String strategy, List<RecommendedGameDto> recommendations) {}
