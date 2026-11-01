package com.gamesUP.gamesUP.dto;

import jakarta.validation.constraints.NotBlank;

public record SaveAuthorRequest(@NotBlank String name) {
}
