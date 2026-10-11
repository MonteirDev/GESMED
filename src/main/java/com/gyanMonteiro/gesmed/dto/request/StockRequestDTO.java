package com.gyanMonteiro.gesmed.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record StockRequestDTO(
        @NotNull(message = "Product id is required")
        UUID productId,

        UUID manufacturerId,

        @NotBlank(message = "Batch number is required")
        String batchNumber,

        @NotNull(message = "Expiry date is required")
        LocalDate expiryDate,

        @NotNull(message = "Purchase date is required")
        LocalDate purchaseDate,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than zero")
        Integer quantity,

        @NotNull(message = "Unit cost is required")
        @Positive(message = "Unit cost must be greater than zero")
        BigDecimal unitCost
) {}