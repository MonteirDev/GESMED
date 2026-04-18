package com.gyanMonteiro.gesmed.mapper;

import com.gyanMonteiro.gesmed.exceptions.ResourceNotFoundException;
import com.gyanMonteiro.gesmed.dto.request.ProductRequestDTO;
import com.gyanMonteiro.gesmed.dto.response.ProductCreateResponseDTO;
import com.gyanMonteiro.gesmed.dto.response.ProductResponseDTO;
import com.gyanMonteiro.gesmed.entity.Manufacturer;
import com.gyanMonteiro.gesmed.entity.Product;
import com.gyanMonteiro.gesmed.repository.ManufacturerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    @Autowired
    ManufacturerRepository ManufacturerRepository;

    public Product toEntity(ProductRequestDTO dto){
        Product product = new Product();
        product.setName(dto.name());
        product.setSku(dto.sku());
        product.setUnitofMeasure(dto.unitofMeasure());
        product.setDosage(dto.dosage());
        product.setActive(true);
        Manufacturer manufacturer = ManufacturerRepository.findById(dto.manufacturerId())
                .orElseThrow(() -> new ResourceNotFoundException("Manufacturer not found"));
        product.setManufacturer(manufacturer);
        return product;
    }

    public ProductResponseDTO toResponse (Product product){
        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getUnitofMeasure(),
                product.getDosage(),
                product.getCreatedAt(),
                product.isActive(),
                product.getManufacturer().getId(),
                product.getManufacturer().getName()
        );
    }

    public ProductCreateResponseDTO toCreateResponse(Product product){
        return new ProductCreateResponseDTO(
                product.getId()
        );
    }
}
