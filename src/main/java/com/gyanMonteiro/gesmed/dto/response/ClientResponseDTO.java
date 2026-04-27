package com.gyanMonteiro.gesmed.dto.response;

import com.gyanMonteiro.gesmed.entity.Client;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ClientResponseDTO(
        UUID id,
        String name,
        String cnpj,
        LocalDateTime createdAt,
        List<ClientAddressResponseDTO> addresses
) {
}
