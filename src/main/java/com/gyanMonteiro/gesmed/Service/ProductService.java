package com.gyanMonteiro.gesmed.Service;

import com.gyanMonteiro.gesmed.Exceptions.ResourceNotFoundException;
import com.gyanMonteiro.gesmed.Mapper.ProductMapper;
import com.gyanMonteiro.gesmed.RequestDTO.ProductRequestDTO;
import com.gyanMonteiro.gesmed.ResponseDTO.ProductCreateResponseDTO;
import com.gyanMonteiro.gesmed.ResponseDTO.ProductResponseDTO;
import com.gyanMonteiro.gesmed.entity.Product;
import com.gyanMonteiro.gesmed.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProductService {
    @Autowired
    private ProductRepository repository;

    @Autowired
    private ProductMapper mapper;

    public ProductCreateResponseDTO create(ProductRequestDTO dto){
        Product product = mapper.toEntity(dto);
        repository.save(product);
        return mapper.toCreateResponse(product);
    }

    public ProductResponseDTO update(UUID id, ProductRequestDTO dto){
        Product product = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manufacturer not found"));
        product.setName(dto.name());
        product.setSku(dto.sku());
        product.setUnitofMeasure(dto.unitofMeasure());
        product.setDosage(dto.dosage());
        return mapper.toResponse(product);
    }

    public ProductResponseDTO findById(UUID id){
        Product product = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manufacturer not found"));
        return mapper.toResponse(product);
    }
}
