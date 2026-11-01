package com.gamesUP.gamesUP.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateClientRequest(
        @NotBlank String name,
        @NotBlank @Email String email
) {
}
