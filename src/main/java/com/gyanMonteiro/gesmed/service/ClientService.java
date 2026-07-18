package com.gyanMonteiro.gesmed.service;

import com.gyanMonteiro.gesmed.dto.request.ClientAddressRequestDTO;
import com.gyanMonteiro.gesmed.dto.request.ClientRequestDTO;
import com.gyanMonteiro.gesmed.dto.request.update.ClientUpdateRequestDTO;
import com.gyanMonteiro.gesmed.dto.response.ClientResponseDTO;
import com.gyanMonteiro.gesmed.entity.Client;
import com.gyanMonteiro.gesmed.entity.ClientAddress;
import com.gyanMonteiro.gesmed.exceptions.ResourceNotFoundException;
import com.gyanMonteiro.gesmed.mapper.ClientMapper;
import com.gyanMonteiro.gesmed.repository.ClientAddressRepository;
import com.gyanMonteiro.gesmed.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ClientService {
    @Autowired
    private ClientRepository repository;

    @Autowired
    private ClientAddressRepository addressRepository;

    @Autowired
    private ClientMapper mapper;
    @Transactional
    public ClientResponseDTO create(ClientRequestDTO dto){
        Client client = mapper.toEntity(dto);
        repository.save(client);
        return mapper.toResponse(client);
    }

    public ClientResponseDTO addAddress(UUID id, ClientAddressRequestDTO dto){
        Client client = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        ClientAddress clientAddress = mapper.toAddressEntity(dto, client);
        if (client.getAddresses().stream().anyMatch(address -> address.isMain())) {
            clientAddress.setMain(false);
        }
        client.getAddresses().add(clientAddress);
        repository.save(client);
        return mapper.toResponse(client);
    }

    public ClientResponseDTO updateAddress(UUID clientId, UUID addressId, ClientAddressRequestDTO dto) {
        Client client = repository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        ClientAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        address.setLabel(dto.label());
        address.setStreet(dto.street());
        address.setNumber(dto.number());
        address.setComplement(dto.complement());
        address.setNeighborhood(dto.neighborhood());
        address.setCity(dto.city());
        address.setState(dto.state());
        address.setZipCode(dto.zipCode());
        repository.save(client);
        return mapper.toResponse(client);
    }

    public void deleteAddress(UUID clientId, UUID addressId) {
        Client client = repository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        ClientAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        client.getAddresses().remove(address);
        repository.save(client);
    }

    public ClientResponseDTO update(UUID id, ClientUpdateRequestDTO dto){
        Client client = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        client.setName(dto.name());
        repository.save(client);
        return mapper.toResponse(client);
    }

    public ClientResponseDTO findById(UUID id){
        Client client = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        return mapper.toResponse(client);
    }

    public List<ClientResponseDTO> findByName(String name){
        return repository.findByNameIgnoreCase(name)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public List<ClientResponseDTO> findByCnpj(String cnpj){
        return repository.findByCnpj(cnpj)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public void delete(UUID id){
        Client client = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        repository.delete(client);
    }

    public List<ClientResponseDTO> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
