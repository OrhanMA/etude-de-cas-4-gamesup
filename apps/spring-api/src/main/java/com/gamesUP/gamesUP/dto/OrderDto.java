package com.gamesUP.gamesUP.dto;

import java.util.Date;
import java.util.List;

public record OrderDto(
        Long id,
        UserDto user,
        Date createdAt,
        boolean paid,
        boolean delivered,
        boolean archived,
        List<OrderLineDto> lines
) {}
