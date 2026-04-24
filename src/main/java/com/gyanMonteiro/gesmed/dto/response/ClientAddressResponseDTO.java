package com.gyanMonteiro.gesmed.dto.response;

import com.gyanMonteiro.gesmed.entity.ClientAddress;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClientAddressResponseDTO(
        UUID id,
        String label,
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state,
        String zip_code,
        boolean is_main,
        LocalDateTime createdAt
) {
    public static ClientAddressResponseDTO from(ClientAddress clientAddress){
        return new ClientAddressResponseDTO(
                clientAddress.getId(),
                clientAddress.getLabel(),
                clientAddress.getStreet(),
                clientAddress.getNumber(),
                clientAddress.getComplement(),
                clientAddress.getNeighborhood(),
                clientAddress.getCity(),
                clientAddress.getState(),
                clientAddress.getZipCode(),
                clientAddress.isMain(),
                clientAddress.getCreatedAt()
        );
    }
}
