package com.gyanMonteiro.gesmed.repository;

import com.gyanMonteiro.gesmed.Mapper.ProductMapper;
import com.gyanMonteiro.gesmed.RequestDTO.ProductRequestDTO;
import com.gyanMonteiro.gesmed.entity.Manufacturer;
import com.gyanMonteiro.gesmed.entity.Product;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
@Import(ProductMapper.class)
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    EntityManager entityManager;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductMapper productMapper;

    @Autowired
    ManufacturerRepository manufacturerRepository;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.initMocks(this);
    }

    private Manufacturer buildEntity(UUID id){
        Manufacturer m = new Manufacturer();
        m.setName("CIMED");
        m.setCnpj("12.345.678/0001-99");
        m.setActive(true);
        m.setCreatedAt(LocalDateTime.now());
        return m;
    }


    @Test
    @DisplayName("Should get Product successfully from DB")
    void findByNameIgnoreCase() {
        UUID manufacturerId = UUID.randomUUID();
        Manufacturer manufacturer = this.manufacturerRepository.save(buildEntity(manufacturerId));
        ProductRequestDTO newProduct = new ProductRequestDTO("Dipirona", "ABC-12345", "mg" , "10mg", manufacturer.getId());
        this.createProduct(newProduct);
        List<Product> result = this.productRepository.findByNameIgnoreCase("Dipirona");
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getName()).isEqualToIgnoringCase("Dipirona");
    }

    @Test
    void findByManufacturerNameIgnoreCase() {
        UUID manufacturerId = UUID.randomUUID();
        Manufacturer manufacturer = this.manufacturerRepository.save(buildEntity(manufacturerId));
        ProductRequestDTO newProduct = new ProductRequestDTO("Dipirona", "ABC-12345", "mg" , "10mg", manufacturer.getId());
        this.createProduct(newProduct);
        List<Product> result = this.productRepository.findByManufacturerNameIgnoreCase("CIMED");
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getManufacturer().getName()).isEqualToIgnoringCase("CIMED");
    }

    private Product createProduct(ProductRequestDTO dto){
        Product newProduct = productMapper.toEntity(dto);
        this.entityManager.persist(newProduct);
        return newProduct;
    }
}