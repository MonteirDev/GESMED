package com.gyanMonteiro.gesmed.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ContractItemResponseDTO(
        UUID id,
        ProductSummaryResponseDTO product,
        BigDecimal unitPrice,
        Integer totalQuantity,
        Integer balanceQuantity,
        LocalDateTime createdAt
) {
}
