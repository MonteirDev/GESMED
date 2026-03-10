package com.gyanMonteiro.gesmed.ResponseDTO;

import com.gyanMonteiro.gesmed.entity.Manufacturer;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductResponseDTO(
        UUID id,
        String name,
        String sku,
        String unitofMeasure,
        String dosage,
        LocalDateTime createdAt,
        boolean active,
        UUID manufacturerId,
        String manufacturerName
) {}
