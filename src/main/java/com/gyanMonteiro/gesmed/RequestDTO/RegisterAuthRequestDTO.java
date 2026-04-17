package com.gyanMonteiro.gesmed.RequestDTO;

import jakarta.validation.constraints.NotBlank;

public record RegisterAuthRequestDTO(
        @NotBlank(message = "Username is required!")
        String username,
        @NotBlank(message = "Password id required!")
        String password
) {}
