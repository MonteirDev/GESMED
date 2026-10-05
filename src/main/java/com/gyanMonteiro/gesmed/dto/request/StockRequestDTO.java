package com.gyanMonteiro.gesmed.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record StockRequestDTO(
        UUID productId,
        UUID manufacturerId,
        String batchNumber,
        LocalDate expiryDate,
        LocalDate purchaseDate,
        Integer quantity,
        BigDecimal unitCost
) {}
