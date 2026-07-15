package com.gyanMonteiro.gesmed.dto.response;

public record ProductSummaryResponseDTO(
        String name,
        String manufacturerName,
        String sku,
        String unitofMeasure,
        String dosage
) {}
