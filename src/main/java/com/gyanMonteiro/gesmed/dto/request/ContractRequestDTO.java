package com.gyanMonteiro.gesmed.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record ContractRequestDTO(
        @NotBlank(message = "Contract number is required!")
        String contractNumber,

        @NotBlank(message = "Start date is required!")
        String startDate,

        @NotBlank(message = "End date is required!")
        String endDate,

        @NotBlank(message = "Id of the client is required!")
        UUID clientId,

        @NotEmpty List<ContractItemsRequestDTO> contractItems

) {}
