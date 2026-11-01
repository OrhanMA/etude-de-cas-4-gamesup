package com.gamesUP.gamesUP.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateOrderLineRequest(
        @NotNull @Positive Long gameId,
        @Positive int quantity,
        @NotNull @PositiveOrZero BigDecimal unitPrice
) {
}
