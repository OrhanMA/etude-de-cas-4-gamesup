package com.gamesUP.gamesUP.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PythonUserInteraction(
        @JsonProperty("user_id") Long userId,
        @JsonProperty("game_id") Long gameId,
        @JsonProperty("interaction_count") int interactionCount
) {}
