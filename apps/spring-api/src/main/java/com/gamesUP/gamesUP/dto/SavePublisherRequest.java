package com.gamesUP.gamesUP.dto;

import jakarta.validation.constraints.NotBlank;

public record SavePublisherRequest(@NotBlank String name) {
}
