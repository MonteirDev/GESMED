package com.gyanMonteiro.gesmed.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record ClientRequestDTO(
        @NotBlank(message = "Name is required!")
        String name,

        @NotNull(message = "CNPJ is required!")
        @Pattern(regexp = "\\d{14}", message = "CNPJ deve conter 14 dígitos")
        String cnpj,

        @NotEmpty List<ClientAddressRequestDTO> addresses
        ) {}
