package com.gyanMonteiro.gesmed.controller;

import com.gyanMonteiro.gesmed.config.security.JwtService;
import com.gyanMonteiro.gesmed.config.security.SecurityConfig;
import com.gyanMonteiro.gesmed.dto.response.ManufacturerSummaryResponseDTO;
import com.gyanMonteiro.gesmed.dto.response.ProductBasicSummaryResponseDTO;
import com.gyanMonteiro.gesmed.dto.response.SupplierPurchaseHistoryResponseDTO;
import com.gyanMonteiro.gesmed.exceptions.ResourceNotFoundException;
import com.gyanMonteiro.gesmed.repository.UserRepository;
import com.gyanMonteiro.gesmed.service.SupplierPurchaseHistoryService;
import com.gyanMonteiro.gesmed.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SupplierPurchaseHistoryController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class SupplierPurchaseHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SupplierPurchaseHistoryService supplierPurchaseHistoryService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private UserRepository userRepository;

    private SupplierPurchaseHistoryResponseDTO buildResponse(UUID id) {
        return new SupplierPurchaseHistoryResponseDTO(
                id,
                new ProductBasicSummaryResponseDTO("Dipirona 500mg", "DIP-12345", "CAIXA", "500mg"),
                new ManufacturerSummaryResponseDTO("Laboratório Exemplo S.A.", true),
                "LT-2026-001",
                100,
                new BigDecimal("12.50"),
                LocalDate.of(2026, 9, 1),
                LocalDateTime.now()
        );
    }

    @Nested
    class FindByIdTests {

        @Test
        @DisplayName("Should return 200 with purchase history details when found")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnPurchaseHistory() throws Exception {
            UUID id = UUID.randomUUID();
            SupplierPurchaseHistoryResponseDTO response = buildResponse(id);

            when(supplierPurchaseHistoryService.findById(id)).thenReturn(response);

            mockMvc.perform(get("/supplier-purchase-history/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.batchNumber").value("LT-2026-001"))
                    .andExpect(jsonPath("$.id").value(id.toString()));
        }

        @Test
        @DisplayName("Should return 404 when purchase history not found")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenNotFound() throws Exception {
            UUID id = UUID.randomUUID();

            when(supplierPurchaseHistoryService.findById(id))
                    .thenThrow(new ResourceNotFoundException("Supplier Purchase History not found"));

            mockMvc.perform(get("/supplier-purchase-history/{id}", id))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 when token is missing")
        void shouldReturn401WhenTokenIsMissing() throws Exception {
            mockMvc.perform(get("/supplier-purchase-history/{id}", UUID.randomUUID()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 403 when role is insufficient")
        @WithMockUser(roles = "USER")
        void shouldReturn403WhenRoleIsInsufficient() throws Exception {
            mockMvc.perform(get("/supplier-purchase-history/{id}", UUID.randomUUID()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class FindByProductTests {

        @Test
        @DisplayName("Should return 200 with purchase history list for product")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnHistoryForProduct() throws Exception {
            UUID productId = UUID.randomUUID();
            List<SupplierPurchaseHistoryResponseDTO> responses = List.of(buildResponse(UUID.randomUUID()));

            when(supplierPurchaseHistoryService.findByProduct(productId)).thenReturn(responses);

            mockMvc.perform(get("/supplier-purchase-history/product/{productId}", productId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("Should return 200 with empty list when product has no purchase history")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnEmptyListWhenNoHistory() throws Exception {
            UUID productId = UUID.randomUUID();

            when(supplierPurchaseHistoryService.findByProduct(productId)).thenReturn(List.of());

            mockMvc.perform(get("/supplier-purchase-history/product/{productId}", productId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    class FindByManufacturerTests {

        @Test
        @DisplayName("Should return 200 with purchase history list for manufacturer")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnHistoryForManufacturer() throws Exception {
            UUID manufacturerId = UUID.randomUUID();
            List<SupplierPurchaseHistoryResponseDTO> responses = List.of(buildResponse(UUID.randomUUID()));

            when(supplierPurchaseHistoryService.findByManufacturer(manufacturerId)).thenReturn(responses);

            mockMvc.perform(get("/supplier-purchase-history/manufacturer/{manufacturerId}", manufacturerId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }
    }

    @Nested
    class FindByManufacturerPaginatedTests {

        @Test
        @DisplayName("Should return 200 with paginated purchase history for manufacturer")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnPaginatedHistoryForManufacturer() throws Exception {
            UUID manufacturerId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 20);
            Page<SupplierPurchaseHistoryResponseDTO> page =
                    new PageImpl<>(List.of(buildResponse(UUID.randomUUID())), pageable, 1);

            when(supplierPurchaseHistoryService.findByManufacturerPaginated(eq(manufacturerId), any()))
                    .thenReturn(page);

            mockMvc.perform(get("/supplier-purchase-history/manufacturer/{manufacturerId}/paginated", manufacturerId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }
    }

    @Nested
    class FindByPeriodTests {

        @Test
        @DisplayName("Should return 200 with purchase history within given period")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnHistoryWithinPeriod() throws Exception {
            LocalDate start = LocalDate.of(2026, 1, 1);
            LocalDate end = LocalDate.of(2026, 12, 31);
            List<SupplierPurchaseHistoryResponseDTO> responses = List.of(buildResponse(UUID.randomUUID()));

            when(supplierPurchaseHistoryService.findByPeriod(start, end)).thenReturn(responses);

            mockMvc.perform(get("/supplier-purchase-history/period")
                            .param("start", "2026-01-01")
                            .param("end", "2026-12-31"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("Should return 400 when start param is missing")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenStartIsMissing() throws Exception {
            mockMvc.perform(get("/supplier-purchase-history/period")
                            .param("end", "2026-12-31"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class GetAverageCostTests {

        @Test
        @DisplayName("Should return 200 with average cost for product in given period")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnAverageCost() throws Exception {
            UUID productId = UUID.randomUUID();
            LocalDate start = LocalDate.of(2026, 1, 1);
            LocalDate end = LocalDate.of(2026, 12, 31);

            when(supplierPurchaseHistoryService.getAveragePurchasePriceByProduct(productId, start, end))
                    .thenReturn(new BigDecimal("12.75"));

            mockMvc.perform(get("/supplier-purchase-history/product/{productId}/average-cost", productId)
                            .param("start", "2026-01-01")
                            .param("end", "2026-12-31"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(12.75));
        }
    }
}