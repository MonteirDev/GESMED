package com.gyanMonteiro.gesmed.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ClientAddressRequestDTO(
        @NotBlank String label,
        @NotBlank String street,
        String number,
        String complement,
        String neighborhood,
        @NotBlank String city,
        @NotBlank String state,
        @NotBlank String zipCode
) {
}
