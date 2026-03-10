package com.gyanMonteiro.gesmed.RequestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record ProductRequestDTO(
        @NotBlank(message = "Name is required!")
        String name,

        @NotBlank(message = "SKU is required!")
        @Pattern(regexp = "[A-Z]+-\\d+", message = "SKU must start with 3 uppercase letters followed by a hyphen and 5 numbers (ex: ABC-12345)")
        String sku,

        @NotBlank(message = "Unit of measure is required!")
        String unitofMeasure,

        @NotBlank(message = "Dosage is required!")
        String dosage,

        @NotNull(message = "ID for Manufacturer is required!")
        UUID manufacturerId
) {}
