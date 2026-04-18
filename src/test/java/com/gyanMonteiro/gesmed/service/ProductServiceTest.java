package com.gyanMonteiro.gesmed.service;

import com.gyanMonteiro.gesmed.exceptions.ResourceNotFoundException;
import com.gyanMonteiro.gesmed.mapper.ProductMapper;
import com.gyanMonteiro.gesmed.dto.request.ProductRequestDTO;
import com.gyanMonteiro.gesmed.dto.response.ProductCreateResponseDTO;
import com.gyanMonteiro.gesmed.dto.response.ProductResponseDTO;
import com.gyanMonteiro.gesmed.entity.Manufacturer;
import com.gyanMonteiro.gesmed.entity.Product;
import com.gyanMonteiro.gesmed.repository.ProductRepository;
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
import java.util.List;
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
        @DisplayName("Should return ProductResponseDTO when id exists")
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

            assertThatThrownBy(() -> service.findById(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Product not found");

            verify(repository).findById(id);
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Should propagate exception thrown by repository")
        void repositoryExceptionShouldBePropagated(){
            UUID id = UUID.randomUUID();
            when(repository.findById(id)).thenThrow(new RuntimeException("Database Error"));
            assertThrows(RuntimeException.class, () -> service.findById(id));
        }
    }

    @Nested
    class UpdateTest {
        @Test
        @DisplayName("Should update product and return updated ProductResponseDTO")
        void shouldUpdateProduct() {
            UUID id = UUID.randomUUID();
            Manufacturer manufacturerEntity = buildManufacturer();
            ProductRequestDTO updateRequest = new ProductRequestDTO("Produto 1", "SKU-00001", "mg", "10mg", manufacturerEntity.getId());
            Product entity = buildEntity(id);

            ProductResponseDTO expectedResponse = new ProductResponseDTO(id,
                    "Produto 1",
                    "SKU-00001",
                    "mg",
                    "10mg",
                    entity.getCreatedAt(),
                    entity.isActive(),
                    manufacturerEntity.getId(),
                    manufacturerEntity.getName());

            when(repository.findById(id)).thenReturn(Optional.of(entity));
            when(mapper.toResponse(entity)).thenReturn(expectedResponse);

            ProductResponseDTO result = service.update(id, updateRequest);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Produto 1");
            assertThat(result.sku()).isEqualTo("SKU-00001");

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

    @Nested
    class DeleteTests {
        @Test
        @DisplayName("Should call repository.deleteById when deleting product")
        void deleteShouldCallDeleteById(){
            UUID id = UUID.randomUUID();
            Product product = new Product();
            product.setId(id);

            when(repository.findById(id)).thenReturn(Optional.of(product));
            service.delete(id);
            verify(repository, times(1)).delete(product);
        }
    }

    @Nested
    class FindAllTests {
        @Test
        @DisplayName("Should return all products mapped to DTO")
        void listAllShouldReturnListMappedToDTO(){
            Manufacturer manufacturer = new Manufacturer();
            manufacturer.setId(UUID.randomUUID());
            manufacturer.setName("Fabricante 1");

            Product p1 = new Product();
            p1.setId(UUID.randomUUID());

            Product p2 = new Product();
            p2.setId(UUID.randomUUID());

            List<Product> products = List.of(p1, p2);
            when(repository.findAll()).thenReturn(products);

            ProductResponseDTO response1 = new ProductResponseDTO(p1.getId(),
                    "Produto 1",
                    "SKU-00001",
                    "mg",
                    "750mg",
                    LocalDateTime.now(),
                    true,
                    manufacturer.getId(),
                    manufacturer.getName());

            ProductResponseDTO response2 = new ProductResponseDTO(p2.getId(),
                    "Produto 2",
                    "SKU-00002",
                    "mg",
                    "750mg",
                    LocalDateTime.now(),
                    true,
                    manufacturer.getId(),
                    manufacturer.getName());

            when(mapper.toResponse(p1)).thenReturn(response1);
            when(mapper.toResponse(p2)).thenReturn(response1);

            List<ProductResponseDTO> result = service.findAll();

            assertEquals(2, result.size());

            verify(repository, times(1)).findAll();
            verify(mapper, times(1)).toResponse(p1);
            verify(mapper, times(1)).toResponse(p2);
        }
    }
}