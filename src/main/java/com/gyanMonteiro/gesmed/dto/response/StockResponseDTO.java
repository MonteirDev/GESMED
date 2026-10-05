package com.gyanMonteiro.gesmed.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record StockResponseDTO(
        UUID id,
        ProductSummaryResponseDTO product,
        String batchNumber,
        LocalDate expiryDate,
        Integer quantity,
        Integer reserveQuantity,
        LocalDateTime createdAt

) {
}
