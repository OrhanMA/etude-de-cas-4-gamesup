package com.gamesUP.gamesUP.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PythonGameRecommendation(
        @JsonProperty("game_id") Long gameId,
        @JsonProperty("game_name") String gameName,
        String genre,
        double score
) {}
