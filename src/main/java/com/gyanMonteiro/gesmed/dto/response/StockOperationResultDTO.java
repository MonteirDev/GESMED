package com.gyanMonteiro.gesmed.dto.response;

import java.util.UUID;

public record StockOperationResultDTO(
        String message,
        UUID productId,
        Integer quantityAffected,
        Integer remainingQuantity
) {
}
