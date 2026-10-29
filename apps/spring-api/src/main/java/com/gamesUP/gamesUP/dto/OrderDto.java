package com.gamesUP.gamesUP.dto;

import java.util.Date;

public record OrderDto(
        Long id,
        UserDto user,
        Date createdAt,
        boolean paid,
        boolean delivered,
        boolean archived
) {}
