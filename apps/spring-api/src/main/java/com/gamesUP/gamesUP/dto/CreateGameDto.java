package com.gamesUP.gamesUP.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record CreateGameDto(
        @NotBlank String name,
        @NotBlank String genre,
        @Positive int editionNumber,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        @Positive Long authorId,
        @Positive Long publisherId
) {}
