package com.gyanMonteiro.gesmed.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginAuthRequestDTO(
        @NotBlank(message = "Username is required!")
        String username,

        @NotBlank(message = "Password is required!")
        String password
) {}
