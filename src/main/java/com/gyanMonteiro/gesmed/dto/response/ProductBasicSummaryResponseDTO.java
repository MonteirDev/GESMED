package com.gyanMonteiro.gesmed.dto.response;

public record ProductBasicSummaryResponseDTO(
        String name,
        String sku,
        String unitOfMeasure,
        String dosage

) {
}
