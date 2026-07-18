package com.gyanMonteiro.gesmed.dto.request.update;

import jakarta.validation.constraints.NotBlank;

public record ManufacturerUpdateRequestDTO(
        @NotBlank(message = "Name is required!")
        String name
) {
}
