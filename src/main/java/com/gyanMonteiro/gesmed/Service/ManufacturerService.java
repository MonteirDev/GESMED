package com.gyanMonteiro.gesmed.Service;

import com.gyanMonteiro.gesmed.Exceptions.ResourceNotFoundException;
import com.gyanMonteiro.gesmed.Mapper.ManufacturerMapper;
import com.gyanMonteiro.gesmed.RequestDTO.ManufacturerRequestDTO;
import com.gyanMonteiro.gesmed.ResponseDTO.ManufacturerCreateResponseDTO;
import com.gyanMonteiro.gesmed.ResponseDTO.ManufacturerResponseDTO;
import com.gyanMonteiro.gesmed.entity.Manufacturer;
import com.gyanMonteiro.gesmed.repository.ManufacturerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ManufacturerService {
    @Autowired
    private ManufacturerRepository repository;

    @Autowired
    private ManufacturerMapper mapper;

    public ManufacturerCreateResponseDTO create(ManufacturerRequestDTO dto){
        Manufacturer manufacturer = mapper.toEntity(dto);
        repository.save(manufacturer);
        return mapper.toCreateResponse(manufacturer);
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

    public void delete(UUID id){
        Manufacturer manufacturer = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manufacturer not found"));
        repository.delete(manufacturer);
    }
}
