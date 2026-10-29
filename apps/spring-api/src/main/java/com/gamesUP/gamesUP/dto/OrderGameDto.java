package com.gamesUP.gamesUP.dto;

import java.math.BigDecimal;

public record OrderGameDto(
        Long id,
        OrderDto order,
        GameDto game,
        int quantity,
        BigDecimal unitPrice
) {}
