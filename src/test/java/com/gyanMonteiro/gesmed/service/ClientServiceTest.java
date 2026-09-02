package com.gyanMonteiro.gesmed.service;

import com.gyanMonteiro.gesmed.dto.request.ClientAddressRequestDTO;
import com.gyanMonteiro.gesmed.dto.request.ClientRequestDTO;
import com.gyanMonteiro.gesmed.dto.request.update.ClientUpdateRequestDTO;
import com.gyanMonteiro.gesmed.dto.response.ClientResponseDTO;
import com.gyanMonteiro.gesmed.entity.Client;
import com.gyanMonteiro.gesmed.exceptions.ResourceNotFoundException;
import com.gyanMonteiro.gesmed.mapper.ClientMapper;
import com.gyanMonteiro.gesmed.repository.ClientRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository repository;

    @Mock
    private ClientMapper mapper;

    @InjectMocks
    private ClientService service;

    private ClientRequestDTO buildRequest() {
        ClientAddressRequestDTO address = new ClientAddressRequestDTO(
                "Almoxarifado Central", "Rua A", "123",
                null, "Centro", "Recife", "PE", "50000000"
        );
        return new ClientRequestDTO("Hospital das Clínicas", "12345678000100", List.of(address));
    }

    private ClientUpdateRequestDTO buildUpdateRequest() {
        return new ClientUpdateRequestDTO("Hospital das Clínicas");
    }

    private Client buildClient() {
        Client client = new Client();
        client.setName("Hospital das Clínicas");
        client.setCnpj("12345678000100");
        client.setAddresses(new ArrayList<>());
        return client;
    }

    private ClientResponseDTO buildResponse(UUID id) {
        return new ClientResponseDTO(
                id,
                "Hospital das Clínicas",
                "12.345.678/0001-00",
                true,
                LocalDateTime.now(),
                List.of()
        );
    }


    @Nested
    class CreateTests{
        @Test
        @DisplayName("Should create product and return ProductCreateResponseDTO")
        void shouldCreateProduct() {
            ClientRequestDTO request = buildRequest();
            Client entity = buildClient();
            UUID id = UUID.randomUUID();
            ClientResponseDTO response = buildResponse(id);

            when(mapper.toEntity(request)).thenReturn(entity);
            when(repository.save(entity)).thenReturn(entity);
            when(mapper.toResponse(entity)).thenReturn(response);

            ClientResponseDTO result = service.create(request);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Hospital das Clínicas");
            verify(mapper).toEntity(request);
            verify(repository).save(entity);
            verify(mapper).toResponse(entity);
        }
    }

    @Nested
    class UpdateTest {
        @Test
        @DisplayName("Should update product and return updated ProductResponseDTO")
        void shouldUpdateProduct() {
            ClientUpdateRequestDTO request = buildUpdateRequest();
            Client entity = buildClient();
            UUID id = UUID.randomUUID();
            ClientResponseDTO response = buildResponse(id);

            when(repository.findById(id)).thenReturn((Optional.of(entity)));
            when(repository.save(entity)).thenReturn(entity);
            when(mapper.toResponse(entity)).thenReturn(response);

            ClientResponseDTO result = service.update(id, request);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Hospital das Clínicas");
            verify(repository).findById(id);
            verify(repository).save(entity);
            verify(mapper).toResponse(entity);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when id does not exist")
        void shouldThrowExceptionWhenIdNotFoundOnUpdate() throws ResourceNotFoundException {
            UUID id = UUID.randomUUID();
            ClientUpdateRequestDTO request = buildUpdateRequest();

            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(id, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Client not found");

            verify(repository, never()).save(any());
        }
    }

    @Nested
    class FindByIdTests {
        @Test
        @DisplayName("Should return ProductResponseDTO when id exists")
        void shouldReturnWhenIdExists() {
            UUID id = UUID.randomUUID();
            Client entity = buildClient();
            ClientResponseDTO response = buildResponse(id);

            when(repository.findById(id)).thenReturn(Optional.of(entity));
            when(mapper.toResponse(entity)).thenReturn(response);

            ClientResponseDTO result = service.findById(id);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Hospital das Clínicas");
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
                    .hasMessage("Client not found");

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
    class FindByNameTests {
        @Test
        @DisplayName("Should return ProductResponseDTO when id exists")
        void shouldReturnWhenIdExists() {
            Client entity = buildClient();
            ClientResponseDTO response = buildResponse(UUID.randomUUID());

            when(repository.findByNameIgnoreCase("Hospital das Clínicas"))
                    .thenReturn(Optional.of(entity));
            when(mapper.toResponse(entity)).thenReturn(response);

            List<ClientResponseDTO> result = service.findByName("Hospital das Clínicas");

            assertThat(result).isNotEmpty();
            assertThat(result).hasSize(1);
            verify(repository).findByNameIgnoreCase("Hospital das Clínicas");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when id does not exist")
        void shouldThrowExceptionWhenIdNotFound() {
            when(repository.findByNameIgnoreCase("Inexistente"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findByName("Inexistente"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Client not found");
        }
    }

    @Nested
    class FindByCnpjTests {
        @Test
        @DisplayName("Should return ProductResponseDTO when id exists")
        void shouldReturnWhenIdExists() {
            Client entity = buildClient();
            ClientResponseDTO response = buildResponse(UUID.randomUUID());

            when(repository.findByCnpj("12345678000100"))
                    .thenReturn(Optional.of(entity));
            when(mapper.toResponse(entity)).thenReturn(response);

            List<ClientResponseDTO> result = service.findByCnpj("12345678000100");

            assertThat(result).isNotEmpty();
            assertThat(result).hasSize(1);
            verify(repository).findByCnpj("12345678000100");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when id does not exist")
        void shouldThrowExceptionWhenIdNotFound() {
            when(repository.findByCnpj("00000008000100"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findByCnpj("00000008000100"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Client not found");
        }
    }

    @Nested
    class DeleteTests {
        @Test
        @DisplayName("Should call repository.deleteById when deleting product")
        void deleteShouldCallDeleteById(){
            UUID id = UUID.randomUUID();
            Client entity = buildClient();

            when(repository.findById(id)).thenReturn(Optional.of(entity));

            service.delete(id);

            verify(repository).findById(id);
            verify(repository).delete(entity);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when id does not exist")
        void shouldThrowExceptionWhenClientNotFoundOnDelete() {
            UUID id = UUID.randomUUID();

            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.delete(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Client not found");

            verify(repository, never()).delete(any());
        }
    }

    @Nested
    class FindAllTests {
        @Test
        @DisplayName("Should return all products mapped to DTO")
        void listAllShouldReturnListMappedToDTO(){
            Client client1 = buildClient();
            Client client2 = buildClient();
            ClientResponseDTO response1 = buildResponse(UUID.randomUUID());
            ClientResponseDTO response2 = buildResponse(UUID.randomUUID());

            when(repository.findAll()).thenReturn(List.of(client1, client2));
            when(mapper.toResponse(client1)).thenReturn(response1);
            when(mapper.toResponse(client2)).thenReturn(response2);

            List<ClientResponseDTO> result = service.findAll();

            assertThat(result).hasSize(2);
            verify(repository).findAll();
            verify(mapper, times(2)).toResponse(any());
        }
    }
}