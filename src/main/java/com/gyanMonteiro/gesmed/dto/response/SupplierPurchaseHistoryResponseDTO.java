package com.gyanMonteiro.gesmed.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record SupplierPurchaseHistoryResponseDTO(
        UUID id,
        ProductBasicSummaryResponseDTO product,
        ManufacturerSummaryResponseDTO manufacture,
        String batchNumber,
        Integer quantity,
        BigDecimal unitCost,
        LocalDate purchaseDate,
        LocalDateTime createdAt
) {
}
