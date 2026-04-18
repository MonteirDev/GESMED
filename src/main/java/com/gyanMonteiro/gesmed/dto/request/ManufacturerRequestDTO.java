package com.gyanMonteiro.gesmed.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ManufacturerRequestDTO(
        @NotBlank(message = "Name is required!")
        String name,

        @NotBlank(message = "CNPJ is required!")
        @Pattern(regexp = "\\d{14}", message = "CNPJ deve conter 14 dígitos")
        String cnpj
) {}
