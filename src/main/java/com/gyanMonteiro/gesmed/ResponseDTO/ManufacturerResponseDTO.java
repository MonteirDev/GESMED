package com.gyanMonteiro.gesmed.ResponseDTO;

import java.time.LocalDateTime;
import java.util.UUID;

public record ManufacturerResponseDTO(
        UUID id,
        String name,
        String cnpj,
        LocalDateTime createdAt,
        boolean active
) { }
