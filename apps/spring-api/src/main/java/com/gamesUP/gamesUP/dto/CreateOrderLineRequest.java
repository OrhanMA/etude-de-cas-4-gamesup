package com.gamesUP.gamesUP.dto;

import jakarta.validation.constraints.Positive;

public record CreateOrderLineRequest(
        @Positive long gameId,
        @Positive int quantity
) {
}
