package com.gyanMonteiro.gesmed.service;

import com.gyanMonteiro.gesmed.dto.request.ClientRequestDTO;
import com.gyanMonteiro.gesmed.dto.response.ClientResponseDTO;
import com.gyanMonteiro.gesmed.entity.Client;
import com.gyanMonteiro.gesmed.exceptions.ResourceNotFoundException;
import com.gyanMonteiro.gesmed.mapper.ClientMapper;
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
    private ClientMapper mapper;
    @Transactional
    public ClientResponseDTO create(ClientRequestDTO dto){
        Client client = mapper.toEntity(dto);
        repository.save(client);
        return mapper.toResponse(client);
    }

    public ClientResponseDTO update(UUID id, ClientRequestDTO dto){
        Client client = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        client.setName(dto.name());
        client.setCnpj(dto.cnpj());
        repository.save(client);
        return mapper.toResponse(client);
    }

    public ClientResponseDTO findById(UUID id){
        Client client = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        return mapper.toResponse(client);
    }

    public List<ClientResponseDTO> findByName(String name){
        Client client = repository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        return List.of(mapper.toResponse(client));
    }

    public List<ClientResponseDTO> findByCnpj(String cnpj){
        Client client = repository.findByNameIgnoreCase(cnpj)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        return List.of(mapper.toResponse(client));
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
