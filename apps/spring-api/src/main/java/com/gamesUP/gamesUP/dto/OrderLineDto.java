package com.gamesUP.gamesUP.dto;

import java.math.BigDecimal;

public record OrderLineDto(Long id, GameDto game, int quantity, BigDecimal unitPrice) {
}
