package com.gyanMonteiro.gesmed.dto.response;

import com.gyanMonteiro.gesmed.enums.ContractStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ContractResponseDTO(
        UUID id,
        String contractNumber,
        LocalDate startDate,
        LocalDate endDate,
        ContractStatus status,
        LocalDateTime createdAt,
        ClientSummaryResponseDTO client,
        List<ContractItemResponseDTO> items
) {
}
