package com.gamesUP.gamesUP.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record PythonCatalogGame(
        @JsonProperty("game_id") Long gameId,
        String name,
        String genre,
        @JsonProperty("edition_number") int editionNumber,
        BigDecimal price,
        String author,
        String publisher
) {}
