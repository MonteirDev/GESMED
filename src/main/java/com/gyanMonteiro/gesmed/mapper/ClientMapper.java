package com.gyanMonteiro.gesmed.mapper;

import com.gyanMonteiro.gesmed.dto.request.ClientAddressRequestDTO;
import com.gyanMonteiro.gesmed.dto.request.ClientRequestDTO;
import com.gyanMonteiro.gesmed.dto.response.ClientAddressResponseDTO;
import com.gyanMonteiro.gesmed.dto.response.ClientResponseDTO;
import com.gyanMonteiro.gesmed.dto.response.ProductResponseDTO;
import com.gyanMonteiro.gesmed.entity.Client;
import com.gyanMonteiro.gesmed.entity.ClientAddress;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClientMapper {
    public Client toEntity(ClientRequestDTO dto){
        Client client = new Client();
        client.setName(dto.name());
        client.setCnpj(dto.cnpj());

        List<ClientAddress> addresses = dto.addresses()
                .stream()
                .map(addressDto -> toAddressEntity(addressDto, client))
                .toList();

        client.setAddresses(addresses);
        return client;
    }

    private ClientAddress toAddressEntity(ClientAddressRequestDTO dto, Client client){
        ClientAddress address = new ClientAddress();
        address.setLabel(dto.label());
        address.setStreet(dto.street());
        address.setNumber(dto.number());
        address.setComplement(dto.complement());
        address.setNeighborhood(dto.neighborhood());
        address.setCity(dto.city());
        address.setState(dto.state());
        address.setZipCode(dto.zipCode());
        address.setMain(true);
        address.setClient(client);
        return address;
    }

    public ClientResponseDTO toResponse (Client client){
        List<ClientAddressResponseDTO> address = client.getAddresses().stream()
                .map(this::toAddressResponse)
                .toList();
        return new ClientResponseDTO(
                client.getId(),
                client.getName(),
                formatCnpj(client.getCnpj()),
                client.getCreatedAt(),
                address
        );
    }

    public ClientAddressResponseDTO toAddressResponse(ClientAddress clientAddress){
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

    private String formatCnpj(String cnpj) {
        if (cnpj == null || cnpj.length() != 14) return cnpj;
        return cnpj.replaceAll("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
    }
}
