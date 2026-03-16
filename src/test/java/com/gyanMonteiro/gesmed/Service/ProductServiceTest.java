package com.gyanMonteiro.gesmed.Service;

import com.gyanMonteiro.gesmed.Exceptions.ResourceNotFoundException;
import com.gyanMonteiro.gesmed.Mapper.ProductMapper;
import com.gyanMonteiro.gesmed.RequestDTO.ProductRequestDTO;
import com.gyanMonteiro.gesmed.ResponseDTO.ManufacturerCreateResponseDTO;
import com.gyanMonteiro.gesmed.ResponseDTO.ProductCreateResponseDTO;
import com.gyanMonteiro.gesmed.ResponseDTO.ProductResponseDTO;
import com.gyanMonteiro.gesmed.entity.Manufacturer;
import com.gyanMonteiro.gesmed.entity.Product;
import com.gyanMonteiro.gesmed.repository.ProductRepository;
import com.sun.java.accessibility.util.GUIInitializedListener;
import jakarta.persistence.Id;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DataJpaTest
@Import(ProductMapper.class)
@ActiveProfiles("test")
class ProductServiceTest {

    @InjectMocks
    private ProductService service;

    @Mock
    private ProductRepository repository;

    @Mock
    private ProductMapper mapper;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.initMocks(this);
    }

    private ProductRequestDTO buildRequest(UUID id){
        return new ProductRequestDTO("Paracetamol", "SKU-12345", "mg", "500mg", id);
    }
    private Manufacturer buildManufacturer(){
        Manufacturer m = new Manufacturer();
        m.setId(UUID.randomUUID());
        m.setName("CIMED");
        m.setCnpj("12.345.678/0001-99");
        m.setActive(true);
        m.setCreatedAt(LocalDateTime.now());
        return m;
    }

    private Product buildEntity(UUID id){
        Product p = new Product();
        p.setName("Paracetamol");
        p.setSku("SKU-12345");
        p.setUnitofMeasure("mg");
        p.setDosage("500mg");
        p.setCreatedAt(LocalDateTime.now());
        p.setActive(true);
        p.setManufacturer(buildManufacturer());
        return p;
    }

    @Nested
    class CreateTests{
        @Test
        @DisplayName("Should create product and return ProductCreateResponseDTO")
        void shouldCreateProduct() {
            UUID id = UUID.randomUUID();
            ProductRequestDTO request = buildRequest(id);
            Product entity = buildEntity(id);
            ProductCreateResponseDTO expectedResponse = new ProductCreateResponseDTO(id);

            when(mapper.toEntity(request)).thenReturn(entity);
            when(repository.save(entity)).thenReturn(entity);
            when(mapper.toCreateResponse(entity)).thenReturn(expectedResponse);

            ProductCreateResponseDTO result = service.create(request);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(id);

            verify(mapper).toEntity(request);
            verify(repository).save(entity);
            verify(mapper).toCreateResponse(entity);


        }

        @Nested
        class UpdateTests {
            @Test
            @DisplayName("Should update product and return updated ProductResponseDTO")
            void shouldUpdateProduct() {
                UUID id = UUID.randomUUID();
                Manufacturer manufacturer = buildManufacturer();
                ProductRequestDTO updateRequest = new ProductRequestDTO("Paracetamol Atualizado", "SKU-99999", "mg", "750mg", manufacturer.getId());
                Product entity = buildEntity(id);
                ProductResponseDTO expectedResponse = new ProductResponseDTO(id, "Paracetamol Atualizado", "SKU-99999", "mg", "750mg", entity.getCreatedAt(), entity.isActive(), entity.getManufacturer().getId(), entity.getManufacturer().getName());

                when(repository.findById(id)).thenReturn(Optional.of(entity));
                when(mapper.toResponse(entity)).thenReturn(expectedResponse);

                ProductResponseDTO result = service.update(id, updateRequest);

                assertThat(result).isNotNull();
                assertThat(result.name()).isEqualTo("Paracetamol Atualizado");
                assertThat(result.sku()).isEqualTo("SKU-99999");
                assertThat(result.manufacturerName()).isEqualTo("CIMED");

                verify(repository).findById(id);
                verify(repository).save(entity);
                verify(mapper).toResponse(entity);
            }

            @Test
            @DisplayName("Should throw ResourceNotFoundException when id does not exist")
            void shouldThrowExceptionWhenIdNotFoundOnUpdate() throws ResourceNotFoundException {
                UUID id = UUID.randomUUID();

                when(repository.findById(id)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> service.update(id, buildRequest(id)))
                        .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessage("Product not found");

                verify(repository).findById(id);
                verify(repository, never()).save(any());
            }
        }
    }


    @Nested
    class FindByIdTests {
        @Test
        @DisplayName("Should return ManufacturerResponseDTO when id exists")
        void shouldReturnWhenIdExists() {
            UUID id = UUID.randomUUID();
            Product entity = buildEntity(id);
            ProductResponseDTO expectedResponse = new ProductResponseDTO(id, "Paracetamol Atualizado", "SKU-99999", "mg", "750mg", entity.getCreatedAt(), entity.isActive(), entity.getManufacturer().getId(), entity.getManufacturer().getName());

            when(repository.findById(id)).thenReturn(Optional.of(entity));
            when(mapper.toResponse(entity)).thenReturn(expectedResponse);

            ProductResponseDTO result = service.findById(id);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(id);
            assertThat(result.name()).isEqualTo("Paracetamol Atualizado");

            verify(repository).findById(id);
            verify(mapper).toResponse(entity);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when id does not exist")
        void shouldThrowExceptionWhenIdNotFound() {
            UUID id = UUID.randomUUID();

            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(id, buildRequest(id )))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Product not found");

            verify(repository).findById(id);
            verify(repository, never()).save(any());
        }
    }
}