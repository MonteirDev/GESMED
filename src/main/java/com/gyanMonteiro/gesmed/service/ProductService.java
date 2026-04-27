package com.gyanMonteiro.gesmed.service;

import com.gyanMonteiro.gesmed.dto.response.ManufacturerResponseDTO;
import com.gyanMonteiro.gesmed.exceptions.ResourceNotFoundException;
import com.gyanMonteiro.gesmed.mapper.ProductMapper;
import com.gyanMonteiro.gesmed.dto.request.ProductRequestDTO;
import com.gyanMonteiro.gesmed.dto.response.ProductResponseDTO;
import com.gyanMonteiro.gesmed.entity.Product;
import com.gyanMonteiro.gesmed.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {
    @Autowired
    private ProductRepository repository;

    @Autowired
    private ProductMapper mapper;

    public ProductResponseDTO create(ProductRequestDTO dto){
        Product product = mapper.toEntity(dto);
        repository.save(product);
        return mapper.toResponse(product);
    }

    public ProductResponseDTO update(UUID id, ProductRequestDTO dto){
        Product product = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        product.setName(dto.name());
        product.setSku(dto.sku());
        product.setUnitofMeasure(dto.unitofMeasure());
        product.setDosage(dto.dosage());
        repository.save(product);
        return mapper.toResponse(product);
    }

    public ProductResponseDTO findById(UUID id){
        Product product = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return mapper.toResponse(product);
    }

    public void delete(UUID id){
        Product product = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        repository.delete(product);
    }
    public List<ProductResponseDTO> findByName(String name){
        return repository.findByNameIgnoreCase(name)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public List<ProductResponseDTO> findByManufacturer(String manufacturerName){
        return repository.findByManufacturerNameIgnoreCase(manufacturerName)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public List<ProductResponseDTO> findAll(){
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
