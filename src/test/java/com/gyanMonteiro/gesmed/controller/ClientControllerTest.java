package com.gyanMonteiro.gesmed.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyanMonteiro.gesmed.config.security.JwtService;
import com.gyanMonteiro.gesmed.config.security.SecurityConfig;
import com.gyanMonteiro.gesmed.dto.request.ClientAddressRequestDTO;
import com.gyanMonteiro.gesmed.dto.request.ClientRequestDTO;
import com.gyanMonteiro.gesmed.dto.response.ClientResponseDTO;
import com.gyanMonteiro.gesmed.exceptions.ResourceNotFoundException;
import com.gyanMonteiro.gesmed.repository.UserRepository;
import com.gyanMonteiro.gesmed.service.ClientService;
import com.gyanMonteiro.gesmed.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClientService clientService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ClientRequestDTO buildRequest() {
        ClientAddressRequestDTO address = new ClientAddressRequestDTO(
                "Almoxarifado Central", "Rua A", "123",
                null, "Centro", "Recife", "PE", "50000000"
        );
        return new ClientRequestDTO("Hospital das Clínicas", "12345678000100", List.of(address));
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
    class CreateTests {

        @Test
        @DisplayName("Should return 201 when client is created")
        @WithMockUser(roles = "ADMIN")
        void shouldCreateClient() throws Exception {
            UUID id = UUID.randomUUID();
            ClientResponseDTO response = buildResponse(id);

            when(clientService.create(any())).thenReturn(response);

            mockMvc.perform(post("/client")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Hospital das Clínicas"))
                    .andExpect(jsonPath("$.id").value(id.toString()));
        }

        @Test
        @DisplayName("Should return 400 when body is invalid")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenBodyIsInvalid() throws Exception {
            ClientRequestDTO invalidRequest = new ClientRequestDTO("", "", List.of());

            mockMvc.perform(post("/client")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when required field is missing")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenRequiredFieldIsMissing() throws Exception {
            String bodyWithoutName = """
                    {
                        "cnpj": "12345678000100",
                        "addresses": []
                    }
                    """;

            mockMvc.perform(post("/client")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bodyWithoutName))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 401 when token is missing")
        void shouldReturn401WhenTokenIsMissing() throws Exception {
            mockMvc.perform(post("/client")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest())))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 403 when role is insufficient")
        @WithMockUser(roles = "USER")
        void shouldReturn403WhenRoleIsInsufficient() throws Exception {
            mockMvc.perform(post("/client")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest())))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class FindByIdTests {
        @Test
        @DisplayName("Should return 200 with client details when found")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnClient() throws Exception {
            UUID id = UUID.randomUUID();
            ClientResponseDTO response = buildResponse(id);

            when(clientService.findById(id)).thenReturn(response);

            mockMvc.perform(get("/client/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Hospital das Clínicas"))
                    .andExpect(jsonPath("$.id").value(id.toString()));
        }
        @Test
        @DisplayName("Should return 404 when client not found")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenNotFound() throws Exception {
            UUID id = UUID.randomUUID();

            when(clientService.findById(id))
                    .thenThrow(new ResourceNotFoundException("Client not found"));

            mockMvc.perform(get("/client/{id}", id))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class UpdateTests {

        @Test
        @DisplayName("Should return 200 with updated data")
        @WithMockUser(roles = "ADMIN")
        void shouldUpdateClient() throws Exception {
            UUID id = UUID.randomUUID();
            ClientResponseDTO response = buildResponse(id);

            when(clientService.update(eq(id), any())).thenReturn(response);

            mockMvc.perform(put("/client/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Hospital das Clínicas"));
        }

        @Test
        @DisplayName("Should return 404 when client not found on update")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenNotFoundOnUpdate() throws Exception {
            UUID id = UUID.randomUUID();

            when(clientService.update(eq(id), any()))
                    .thenThrow(new ResourceNotFoundException("Client not found"));

            mockMvc.perform(put("/client/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest())))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class DeleteTests {

        @Test
        @DisplayName("Should return 204 when client is deleted")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn204WhenClientIsDeleted() throws Exception {
            UUID id = UUID.randomUUID();

            doNothing().when(clientService).delete(id);

            mockMvc.perform(delete("/client/{id}", id))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Should return 404 when client not found on delete")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenNotFoundOnDelete() throws Exception {
            UUID id = UUID.randomUUID();

            doThrow(new ResourceNotFoundException("Client not found"))
                    .when(clientService).delete(id);

            mockMvc.perform(delete("/client/{id}", id))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class ListAllTests {

        @Test
        @DisplayName("Should return 200 with list of all clients")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnAllClients() throws Exception {
            List<ClientResponseDTO> responses = List.of(
                    buildResponse(UUID.randomUUID()),
                    buildResponse(UUID.randomUUID())
            );

            when(clientService.findAll()).thenReturn(responses);

            mockMvc.perform(get("/client"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        @DisplayName("Should return 200 filtering by name")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnClientFilteredByName() throws Exception {
            List<ClientResponseDTO> responses = List.of(buildResponse(UUID.randomUUID()));

            when(clientService.findByName("Hospital das Clínicas")).thenReturn(responses);

            mockMvc.perform(get("/client").param("name", "Hospital das Clínicas"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("Should return 200 filtering by cnpj")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnClientFilteredByCnpj() throws Exception {
            List<ClientResponseDTO> responses = List.of(buildResponse(UUID.randomUUID()));

            when(clientService.findByCnpj("12345678000100")).thenReturn(responses);

            mockMvc.perform(get("/client").param("cnpj", "12345678000100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }
    }
}