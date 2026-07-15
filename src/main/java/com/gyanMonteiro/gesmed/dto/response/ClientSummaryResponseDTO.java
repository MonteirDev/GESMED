package com.gyanMonteiro.gesmed.dto.response;

public record ClientSummaryResponseDTO(
        String name,
        String cnpj,
        boolean active
) {}
