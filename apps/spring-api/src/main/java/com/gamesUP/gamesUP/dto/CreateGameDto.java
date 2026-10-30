package com.gamesUP.gamesUP.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateGameDto(
        @NotBlank String name,
        @NotBlank String genre,
        @Positive int editionNumber,
        @Positive Long authorId,
        @Positive Long publisherId
) {}
