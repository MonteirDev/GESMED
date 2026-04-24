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
    public static ClientResponseDTO from(Client client){
        List<ClientAddressResponseDTO> addresses = client.getAddresses().stream()
                .map(ClientAddressResponseDTO::from)
                .toList();
        return new ClientResponseDTO(
                client.getId(),
                client.getName(),
                client.getCnpj(),
                client.getCreatedAt(),
                addresses
        );
    }
}
