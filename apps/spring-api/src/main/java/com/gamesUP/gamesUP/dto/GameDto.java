package com.gamesUP.gamesUP.dto;

import java.math.BigDecimal;

public record GameDto(
        Long id,
        String name,
        String genre,
        int editionNumber,
        BigDecimal price,
        AuthorDto author,
        PublisherDto publisher
) {}
