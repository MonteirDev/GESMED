package com.gyanMonteiro.gesmed.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyanMonteiro.gesmed.config.security.JwtService;
import com.gyanMonteiro.gesmed.config.security.SecurityConfig;
import com.gyanMonteiro.gesmed.dto.request.ContractRequestDTO;
import com.gyanMonteiro.gesmed.dto.response.ContractResponseDTO;
import com.gyanMonteiro.gesmed.enums.ContractStatus;
import com.gyanMonteiro.gesmed.exceptions.ResourceNotFoundException;
import com.gyanMonteiro.gesmed.repository.UserRepository;
import com.gyanMonteiro.gesmed.service.ContractService;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContractController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class ContractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContractService contractService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ContractRequestDTO buildRequest() {
        return new ContractRequestDTO(
                "CONT-2026-001", "2026-01-01", "2026-12-31", UUID.randomUUID(), List.of()
        );
    }

    private ContractResponseDTO buildResponse(UUID id) {
        return new ContractResponseDTO(
                id,
                "CONT-2026-001",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                ContractStatus.ACTIVE,
                null,
                null,
                List.of()
        );
    }

    @Nested
    class CreateTests {

        @Test
        @DisplayName("Should return 201 when contract is created")
        @WithMockUser(roles = "ADMIN")
        void shouldCreateContract() throws Exception {
            UUID id = UUID.randomUUID();
            ContractResponseDTO response = buildResponse(id);

            when(contractService.create(any())).thenReturn(response);

            mockMvc.perform(post("/contract")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.contractNumber").value("CONT-2026-001"))
                    .andExpect(jsonPath("$.id").value(id.toString()));
        }

        @Test
        @DisplayName("Should return 400 when body is invalid")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenBodyIsInvalid() throws Exception {
            ContractRequestDTO invalidRequest = new ContractRequestDTO("", "", "", null, List.of());

            mockMvc.perform(post("/contract")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when required field is missing")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenRequiredFieldIsMissing() throws Exception {
            String bodyWithoutContractNumber = """
                    {
                        "startDate": "2026-01-01",
                        "endDate": "2026-12-31",
                        "clientId": "%s",
                        "items": []
                    }
                    """.formatted(UUID.randomUUID());

            mockMvc.perform(post("/contract")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bodyWithoutContractNumber))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 401 when token is missing")
        void shouldReturn401WhenTokenIsMissing() throws Exception {
            mockMvc.perform(post("/contract")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest())))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 403 when role is insufficient")
        @WithMockUser(roles = "USER")
        void shouldReturn403WhenRoleIsInsufficient() throws Exception {
            mockMvc.perform(post("/contract")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest())))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class FindByIdTests {
        @Test
        @DisplayName("Should return 200 with contract details when found")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnContract() throws Exception {
            UUID id = UUID.randomUUID();
            ContractResponseDTO response = buildResponse(id);

            when(contractService.findById(id)).thenReturn(response);

            mockMvc.perform(get("/contract/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.contractNumber").value("CONT-2026-001"))
                    .andExpect(jsonPath("$.id").value(id.toString()));
        }

        @Test
        @DisplayName("Should return 404 when contract not found")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenNotFound() throws Exception {
            UUID id = UUID.randomUUID();

            when(contractService.findById(id))
                    .thenThrow(new ResourceNotFoundException("Contract not found"));

            mockMvc.perform(get("/contract/{id}", id))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class UpdateEndDateTests {

        @Test
        @DisplayName("Should return 200 with updated data")
        @WithMockUser(roles = "ADMIN")
        void shouldUpdateEndDate() throws Exception {
            UUID id = UUID.randomUUID();
            ContractResponseDTO response = buildResponse(id);

            when(contractService.updateEndDate(eq(id), any())).thenReturn(response);

            mockMvc.perform(patch("/contract/{id}/end-date", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.contractNumber").value("CONT-2026-001"));
        }

        @Test
        @DisplayName("Should return 404 when contract not found on update")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenNotFoundOnUpdate() throws Exception {
            UUID id = UUID.randomUUID();

            when(contractService.updateEndDate(eq(id), any()))
                    .thenThrow(new ResourceNotFoundException("Contract not found"));

            mockMvc.perform(patch("/contract/{id}/end-date", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest())))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class SetStatusCancelledTests {

        @Test
        @DisplayName("Should return 200 when contract is cancelled")
        @WithMockUser(roles = "ADMIN")
        void shouldCancelContract() throws Exception {
            UUID id = UUID.randomUUID();
            ContractResponseDTO response = buildResponse(id);

            when(contractService.setStatusCancelled(id)).thenReturn(response);

            mockMvc.perform(patch("/contract/{id}/cancel", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()));
        }

        @Test
        @DisplayName("Should return 404 when contract not found on cancel")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenNotFoundOnCancel() throws Exception {
            UUID id = UUID.randomUUID();

            when(contractService.setStatusCancelled(id))
                    .thenThrow(new ResourceNotFoundException("Contract not found"));

            mockMvc.perform(patch("/contract/{id}/cancel", id))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class SetStatusSuspendedTests {

        @Test
        @DisplayName("Should return 200 when contract is suspended")
        @WithMockUser(roles = "ADMIN")
        void shouldSuspendContract() throws Exception {
            UUID id = UUID.randomUUID();
            ContractResponseDTO response = buildResponse(id);

            when(contractService.setStatusSuspended(id)).thenReturn(response);

            mockMvc.perform(patch("/contract/{id}/suspend", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()));
        }

        @Test
        @DisplayName("Should return 404 when contract not found on suspend")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenNotFoundOnSuspend() throws Exception {
            UUID id = UUID.randomUUID();

            when(contractService.setStatusSuspended(id))
                    .thenThrow(new ResourceNotFoundException("Contract not found"));

            mockMvc.perform(patch("/contract/{id}/suspend", id))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class SetStatusActivateTests {

        @Test
        @DisplayName("Should return 200 when contract is activated")
        @WithMockUser(roles = "ADMIN")
        void shouldActivateContract() throws Exception {
            UUID id = UUID.randomUUID();
            ContractResponseDTO response = buildResponse(id);

            when(contractService.setStatusActive(id)).thenReturn(response);

            mockMvc.perform(patch("/contract/{id}/activate", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()));
        }

        @Test
        @DisplayName("Should return 404 when contract not found on activate")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenNotFoundOnActivate() throws Exception {
            UUID id = UUID.randomUUID();

            when(contractService.setStatusActive(id))
                    .thenThrow(new ResourceNotFoundException("Contract not found"));

            mockMvc.perform(patch("/contract/{id}/activate", id))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class ListAllTests {

        @Test
        @DisplayName("Should return 200 with list of all contracts")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnAllContracts() throws Exception {
            List<ContractResponseDTO> responses = List.of(
                    buildResponse(UUID.randomUUID()),
                    buildResponse(UUID.randomUUID())
            );

            when(contractService.findAll()).thenReturn(responses);

            mockMvc.perform(get("/contract"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        @DisplayName("Should return 200 filtering by client id")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnContractsFilteredByClientId() throws Exception {
            UUID clientId = UUID.randomUUID();
            List<ContractResponseDTO> responses = List.of(buildResponse(UUID.randomUUID()));

            when(contractService.findByClientId(clientId)).thenReturn(responses);

            mockMvc.perform(get("/contract").param("cliendId", clientId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("Should return 200 filtering by status")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnContractsFilteredByStatus() throws Exception {
            List<ContractResponseDTO> responses = List.of(buildResponse(UUID.randomUUID()));

            when(contractService.findByStatus(ContractStatus.ACTIVE)).thenReturn(responses);

            mockMvc.perform(get("/contract").param("status", "ACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("Should return 200 filtering by contract number")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnContractsFilteredByContractNumber() throws Exception {
            List<ContractResponseDTO> responses = List.of(buildResponse(UUID.randomUUID()));

            when(contractService.findByContractNumber("CONT-2026-001")).thenReturn(responses);

            mockMvc.perform(get("/contract").param("contractNumber", "CONT-2026-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }
    }
}