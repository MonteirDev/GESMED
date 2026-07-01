package com.gyanMonteiro.gesmed.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ClientResponseDTO(
        UUID id,
        String name,
        String cnpj,
        boolean active,
        LocalDateTime createdAt,
        List<ClientAddressResponseDTO> addresses
) {
}
