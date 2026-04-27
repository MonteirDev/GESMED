package com.gyanMonteiro.gesmed.service;

import com.gyanMonteiro.gesmed.exceptions.ResourceNotFoundException;
import com.gyanMonteiro.gesmed.mapper.ManufacturerMapper;
import com.gyanMonteiro.gesmed.dto.request.ManufacturerRequestDTO;
import com.gyanMonteiro.gesmed.dto.response.ManufacturerResponseDTO;
import com.gyanMonteiro.gesmed.entity.Manufacturer;
import com.gyanMonteiro.gesmed.repository.ManufacturerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ManufacturerService {
    @Autowired
    private ManufacturerRepository repository;

    @Autowired
    private ManufacturerMapper mapper;

    public ManufacturerResponseDTO create(ManufacturerRequestDTO dto){
        Manufacturer manufacturer = mapper.toEntity(dto);
        repository.save(manufacturer);
        return mapper.toResponse(manufacturer);
    }

    public ManufacturerResponseDTO update(UUID id, ManufacturerRequestDTO dto){
        Manufacturer manufacturer = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manufacturer not found"));
        manufacturer.setName(dto.name());
        manufacturer.setCnpj(dto.cnpj());
        repository.save(manufacturer);
        return mapper.toResponse(manufacturer);
    }
    public ManufacturerResponseDTO findById(UUID id){
        Manufacturer manufacturer = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manufacturer not found"));
        return mapper.toResponse(manufacturer);
    }
    public List<ManufacturerResponseDTO> findByName(String name){
        return repository.findByNameIgnoreCase(name)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public List<ManufacturerResponseDTO> findByCnpj(String cnpj){
        return repository.findByCnpj(cnpj)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public void delete(UUID id){
        Manufacturer manufacturer = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manufacturer not found"));
        repository.delete(manufacturer);
    }

    public List<ManufacturerResponseDTO> findAll(){
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
