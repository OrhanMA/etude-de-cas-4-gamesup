package com.gamesUP.gamesUP.dto;

import jakarta.validation.Valid;

import java.util.List;

public record UpdateOrderRequest(
        boolean paid,
        boolean delivered,
        boolean archived,
        List<@Valid CreateOrderLineRequest> lines
) {
}
