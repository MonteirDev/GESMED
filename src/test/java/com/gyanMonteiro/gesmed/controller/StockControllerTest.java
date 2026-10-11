package com.gyanMonteiro.gesmed.controller;

import com.gyanMonteiro.gesmed.config.security.JwtService;
import com.gyanMonteiro.gesmed.config.security.SecurityConfig;
import com.gyanMonteiro.gesmed.dto.response.StockOperationResultDTO;
import com.gyanMonteiro.gesmed.dto.response.StockResponseDTO;
import com.gyanMonteiro.gesmed.exceptions.ResourceNotFoundException;
import com.gyanMonteiro.gesmed.repository.UserRepository;
import com.gyanMonteiro.gesmed.service.StockService;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StockController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StockService stockService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private UserRepository userRepository;

    private String buildRequestJson() {
        return """
                {
                    "productId": "%s",
                    "manufacturerId": "%s",
                    "batchNumber": "LT-2026-001",
                    "expiryDate": "2026-12-15",
                    "purchaseDate": "2026-09-01",
                    "quantity": 100,
                    "unitCost": 12.50
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID());
    }

    private StockResponseDTO buildResponse(UUID id) {
        return new StockResponseDTO(
                id, null, "LT-2026-001", LocalDate.of(2026, 12, 15),
                100, 0, LocalDateTime.now()
        );
    }

    @Nested
    class CreateStockTests {

        @Test
        @DisplayName("Should return 201 when stock is created")
        @WithMockUser(roles = "ADMIN")
        void shouldCreateStock() throws Exception {
            UUID id = UUID.randomUUID();
            StockResponseDTO response = buildResponse(id);

            when(stockService.createEntry(any())).thenReturn(response);

            mockMvc.perform(post("/stock")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(buildRequestJson()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.batchNumber").value("LT-2026-001"))
                    .andExpect(jsonPath("$.id").value(id.toString()));
        }

        @Test
        @DisplayName("Should return 400 when body is invalid")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenBodyIsInvalid() throws Exception {
            String invalidBody = """
                    {
                        "batchNumber": ""
                    }
                    """;

            mockMvc.perform(post("/stock")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 401 when token is missing")
        void shouldReturn401WhenTokenIsMissing() throws Exception {
            mockMvc.perform(post("/stock")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(buildRequestJson()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 403 when role is insufficient")
        @WithMockUser(roles = "USER")
        void shouldReturn403WhenRoleIsInsufficient() throws Exception {
            mockMvc.perform(post("/stock")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(buildRequestJson()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class GetStockDetailsTests {

        @Test
        @DisplayName("Should return 200 with stock details when found")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnStock() throws Exception {
            UUID id = UUID.randomUUID();
            StockResponseDTO response = buildResponse(id);

            when(stockService.findById(id)).thenReturn(response);

            mockMvc.perform(get("/stock/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.batchNumber").value("LT-2026-001"))
                    .andExpect(jsonPath("$.id").value(id.toString()));
        }

        @Test
        @DisplayName("Should return 404 when stock not found")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenNotFound() throws Exception {
            UUID id = UUID.randomUUID();

            when(stockService.findById(id))
                    .thenThrow(new ResourceNotFoundException("Stock not found"));

            mockMvc.perform(get("/stock/{id}", id))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 when token is missing")
        void shouldReturn401WhenTokenIsMissing() throws Exception {
            mockMvc.perform(get("/stock/{id}", UUID.randomUUID()))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class GetStockByProductTests {

        @Test
        @DisplayName("Should return 200 with list of batches for product")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnBatchesForProduct() throws Exception {
            UUID productId = UUID.randomUUID();
            List<StockResponseDTO> responses = List.of(buildResponse(UUID.randomUUID()));

            when(stockService.findByProduct(productId)).thenReturn(responses);

            mockMvc.perform(get("/stock/product/{productId}", productId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("Should return 200 with empty list when product has no batches")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnEmptyListWhenNoBatches() throws Exception {
            UUID productId = UUID.randomUUID();

            when(stockService.findByProduct(productId)).thenReturn(List.of());

            mockMvc.perform(get("/stock/product/{productId}", productId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    class GetAvailableQuantityProductTests {

        @Test
        @DisplayName("Should return 200 with available quantity")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnAvailableQuantity() throws Exception {
            UUID productId = UUID.randomUUID();

            when(stockService.getAvailableQuantity(productId)).thenReturn(450);

            mockMvc.perform(get("/stock/product/{productId}/available", productId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(450));
        }
    }

    @Nested
    class GetStockByProductFefoTests {

        @Test
        @DisplayName("Should return 200 with batches ordered by expiry date")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnBatchesOrderedByExpiryDate() throws Exception {
            UUID productId = UUID.randomUUID();
            List<StockResponseDTO> responses = List.of(
                    buildResponse(UUID.randomUUID()),
                    buildResponse(UUID.randomUUID())
            );

            when(stockService.findAvailableByProductFEFO(productId)).thenReturn(responses);

            mockMvc.perform(get("/stock/product/{productId}/fefo", productId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }
    }

    @Nested
    class GetStockExpiringSoonTests {

        @Test
        @DisplayName("Should return 200 with batches expiring before given date")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnExpiringBatches() throws Exception {
            List<StockResponseDTO> responses = List.of(buildResponse(UUID.randomUUID()));

            when(stockService.findExpiringSoon(LocalDate.of(2026, 11, 1))).thenReturn(responses);

            mockMvc.perform(get("/stock/expiring-soon").param("limitDate", "2026-11-01"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("Should return 400 when limitDate param is missing")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenLimitDateIsMissing() throws Exception {
            mockMvc.perform(get("/stock/expiring-soon"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class DispatchStockTests {

        @Test
        @DisplayName("Should return 200 when stock is dispatched")
        @WithMockUser(roles = "ADMIN")
        void shouldDispatchStock() throws Exception {
            UUID productId = UUID.randomUUID();
            StockOperationResultDTO response = new StockOperationResultDTO(
                    "Dispatch completed successfully", productId, 60, 40
            );

            when(stockService.dispatchStock(eq(productId), eq(60))).thenReturn(response);

            mockMvc.perform(patch("/stock/{productId}/dispatch", productId)
                            .param("quantity", "60"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Dispatch completed successfully"))
                    .andExpect(jsonPath("$.quantityAffected").value(60))
                    .andExpect(jsonPath("$.remainingQuantity").value(40));
        }

        @Test
        @DisplayName("Should return 401 when token is missing")
        void shouldReturn401WhenTokenIsMissing() throws Exception {
            mockMvc.perform(patch("/stock/{productId}/dispatch", UUID.randomUUID())
                            .param("quantity", "60"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 403 when role is insufficient")
        @WithMockUser(roles = "USER")
        void shouldReturn403WhenRoleIsInsufficient() throws Exception {
            mockMvc.perform(patch("/stock/{productId}/dispatch", UUID.randomUUID())
                            .param("quantity", "60"))
                    .andExpect(status().isForbidden());
        }
    }
}