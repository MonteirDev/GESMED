package com.gyanMonteiro.gesmed.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

public record ContractItemsRequestDTO(
        BigDecimal unitPrice,
        int totalQuantity,
        int balanceQuantity,
        UUID productId,
        UUID contractId
) {
}
