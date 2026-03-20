package com.gyanMonteiro.gesmed.Service;

import com.gyanMonteiro.gesmed.Exceptions.ResourceNotFoundException;
import com.gyanMonteiro.gesmed.Mapper.ManufacturerMapper;
import com.gyanMonteiro.gesmed.RequestDTO.ManufacturerRequestDTO;
import com.gyanMonteiro.gesmed.ResponseDTO.ManufacturerCreateResponseDTO;
import com.gyanMonteiro.gesmed.ResponseDTO.ManufacturerResponseDTO;
import com.gyanMonteiro.gesmed.entity.Manufacturer;
import com.gyanMonteiro.gesmed.repository.ManufacturerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
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
import static org.mockito.Mockito.*;

@DataJpaTest
@Import(ManufacturerMapper.class)
@ActiveProfiles("test")
class ManufacturerServiceTest {
    @InjectMocks
    private ManufacturerService service;

    @Mock
    private ManufacturerRepository repository;

    @Mock
    private ManufacturerMapper mapper;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.initMocks(this);
    }

    private ManufacturerRequestDTO buildRequest(){
        return new ManufacturerRequestDTO("CIMED", "12.345.678/0001-99");
    }

    private Manufacturer buildEntity(UUID id){
        Manufacturer m = new Manufacturer();
        m.setName("CIMED");
        m.setCnpj("12.345.678/0001-99");
        m.setActive(true);
        m.setCreatedAt(LocalDateTime.now());
        return m;
    }


    @Nested
    class CreateTests {
        @Test
        @DisplayName("Should create manufacturer and return ManufacturerCreateResponseDTO")
        void shouldCreateManufacturer() {
            UUID id = UUID.randomUUID();
            ManufacturerRequestDTO request = buildRequest();
            Manufacturer entity = buildEntity(id);
            ManufacturerCreateResponseDTO expectedResponse = new ManufacturerCreateResponseDTO(id);

            when(mapper.toEntity(request)).thenReturn(entity);
            when(mapper.toCreateResponse(entity)).thenReturn(expectedResponse);

            ManufacturerCreateResponseDTO result = service.create(request);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(id);

            verify(mapper).toEntity(request);
            verify(repository).save(entity);
            verify(mapper).toCreateResponse(entity);
        }
    }

    @Nested
    class UpdateTests {
        @Test
        @DisplayName("Should update manufacturer and return updated ManufacturerResponseDTO")
        void shouldUpdateManufacturer() {
            UUID id = UUID.randomUUID();
            ManufacturerRequestDTO updateRequest = new ManufacturerRequestDTO("Novo nome", "98.765.432/0001-11");
            Manufacturer entity = buildEntity(id);
            ManufacturerResponseDTO expectedResponse = new ManufacturerResponseDTO(id, "Novo nome", "98.765.432/0001-11", entity.getCreatedAt(), true);

            when(repository.findById(id)).thenReturn(Optional.of(entity));
            when(mapper.toResponse(entity)).thenReturn(expectedResponse);

            ManufacturerResponseDTO result = service.update(id, updateRequest);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Novo nome");
            assertThat(result.cnpj()).isEqualTo("98.765.432/0001-11");

            verify(repository).findById(id);
            verify(repository).save(entity);
            verify(mapper).toResponse(entity);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when id does not exist")
        void shouldThrowExceptionWhenIdNotFoundOnUpdate() throws ResourceNotFoundException {
            UUID id = UUID.randomUUID();

            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(id, buildRequest()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Manufacturer not found");

            verify(repository).findById(id);
            verify(repository, never()).save(any());
            }
        }

    @Nested
    class FindByIdTests {
        @Test
        @DisplayName("Should return ManufacturerResponseDTO when id exists")
        void shouldReturnWhenIdExists() {
            UUID id = UUID.randomUUID();
            Manufacturer entity = buildEntity(id);
            ManufacturerResponseDTO expectedResponse = new ManufacturerResponseDTO(id, entity.getName(), entity.getCnpj(), entity.getCreatedAt(), entity.isActive());

            when(repository.findById(id)).thenReturn(Optional.of(entity));
            when(mapper.toResponse(entity)).thenReturn(expectedResponse);

            ManufacturerResponseDTO result = service.findById(id);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(id);
            assertThat(result.name()).isEqualTo("CIMED");

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
                    .hasMessage("Manufacturer not found");

            verify(repository).findById(id);
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Should propagate exception thrown by repository")
        void repositoryExceptionShouldBePropagated(){
            UUID id = UUID.randomUUID();
            when(repository.findById(id)).thenThrow(new RuntimeException("Database error"));
            assertThrows(RuntimeException.class, () -> service.findById(id));
        }
    }
    @Nested
    class DeleteTests {
        @Test
        @DisplayName("Should call repository.deleteById when deleting manufacturer")
        void deleteShouldCallDeleteById(){
            UUID id = UUID.randomUUID();
            Manufacturer manufacturer = new Manufacturer();
            manufacturer.setId(id);

            when(repository.findById(id)).thenReturn(Optional.of(manufacturer));
            service.delete(id);
            verify(repository, times(1)).delete(manufacturer);
        }
    }

    @Nested
    class FindAllTests {
        @Test
        @DisplayName("Should return all manufacturers mapped to DTO")
        void listAllShouldReturnListMappedToDTO(){
            Manufacturer m1 = new Manufacturer();
            m1.setId(UUID.randomUUID());

            Manufacturer m2 = new Manufacturer();
            m2.setId(UUID.randomUUID());

            List<Manufacturer> manufacturers = List.of(m1, m2);
            when(repository.findAll()).thenReturn(manufacturers);

            ManufacturerResponseDTO response1 = new ManufacturerResponseDTO(
                    m1.getId(), "Fabricante A", "11.111.111/0001-11", LocalDateTime.now(), true
            );
            ManufacturerResponseDTO response2 = new ManufacturerResponseDTO(
                    m2.getId(), "Fabricante B", "22.222.222/0001-22", LocalDateTime.now(), true
            );

            when(mapper.toResponse(m1)).thenReturn(response1);
            when(mapper.toResponse(m2)).thenReturn(response2);

            List<ManufacturerResponseDTO> result = service.findAll();

            assertEquals(2, result.size());

            verify(repository, times(1)).findAll();
            verify(mapper, times(1)).toResponse(m1);
            verify(mapper, times(1)).toResponse(m2);
        }
    }
}