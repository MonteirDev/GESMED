package com.gyanMonteiro.gesmed.dto.request.update;

import jakarta.validation.constraints.NotBlank;

public record ClientUpdateRequestDTO(
        @NotBlank(message = "Name is required!")
        String name
) {
}
