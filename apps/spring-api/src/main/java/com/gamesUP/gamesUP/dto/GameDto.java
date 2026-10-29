package com.gamesUP.gamesUP.dto;

public record GameDto(
        Long id,
        String name,
        String genre,
        int editionNumber,
        AuthorDto author,
        PublisherDto publisher
) {}
