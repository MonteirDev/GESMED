package com.gyanMonteiro.gesmed.service;

import com.gyanMonteiro.gesmed.dto.request.StockRequestDTO;
import com.gyanMonteiro.gesmed.dto.response.ManufacturerSummaryResponseDTO;
import com.gyanMonteiro.gesmed.dto.response.ProductBasicSummaryResponseDTO;
import com.gyanMonteiro.gesmed.dto.response.SupplierPurchaseHistoryResponseDTO;
import com.gyanMonteiro.gesmed.entity.Manufacturer;
import com.gyanMonteiro.gesmed.entity.Product;
import com.gyanMonteiro.gesmed.entity.Stock;
import com.gyanMonteiro.gesmed.entity.SupplierPurchaseHistory;
import com.gyanMonteiro.gesmed.exceptions.ResourceNotFoundException;
import com.gyanMonteiro.gesmed.mapper.SupplierPurchaseHistoryMapper;
import com.gyanMonteiro.gesmed.repository.SupplierPurchaseHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplierPurchaseHistoryServiceTest {

    @Mock
    private SupplierPurchaseHistoryRepository repository;

    @Mock
    private SupplierPurchaseHistoryMapper mapper;

    @InjectMocks
    private SupplierPurchaseHistoryService service;

    private Manufacturer buildManufacturer() {
        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setId(UUID.randomUUID());
        manufacturer.setName("Laboratório Exemplo S.A.");
        manufacturer.setActive(true);
        return manufacturer;
    }

    private Product buildProduct(Manufacturer manufacturer) {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Dipirona 500mg");
        product.setSku("DIP-12345");
        product.setManufacturer(manufacturer);
        return product;
    }

    private Stock buildStock(Product product) {
        Stock stock = new Stock();
        stock.setId(UUID.randomUUID());
        stock.setBatchNumber("LT-2026-001");
        stock.setProduct(product);
        return stock;
    }

    private SupplierPurchaseHistory buildEntity(Product product, Manufacturer manufacturer, Stock stock) {
        SupplierPurchaseHistory entity = new SupplierPurchaseHistory();
        entity.setId(UUID.randomUUID());
        entity.setBatchNumber("LT-2026-001");
        entity.setQuantity(100);
        entity.setUnitPrice(new BigDecimal("12.50"));
        entity.setPurchaseDate(LocalDate.of(2026, 9, 1));
        entity.setCreatedAt(LocalDateTime.now());
        entity.setProduct(product);
        entity.setManufacturer(manufacturer);
        entity.setStock(stock);
        return entity;
    }

    private SupplierPurchaseHistoryResponseDTO buildResponse(SupplierPurchaseHistory entity) {
        return new SupplierPurchaseHistoryResponseDTO(
                entity.getId(),
                new ProductBasicSummaryResponseDTO(
                        entity.getProduct().getName(), entity.getProduct().getSku(), null, null
                ),
                new ManufacturerSummaryResponseDTO(
                        entity.getManufacturer().getName(), entity.getManufacturer().isActive()
                ),
                entity.getBatchNumber(),
                entity.getQuantity(),
                entity.getUnitPrice(),
                entity.getPurchaseDate(),
                entity.getCreatedAt()
        );
    }

    private StockRequestDTO buildRequest(UUID productId, UUID manufacturerId) {
        return new StockRequestDTO(
                productId, manufacturerId, "LT-2026-001",
                LocalDate.of(2026, 12, 15), LocalDate.of(2026, 9, 1),
                100, new BigDecimal("12.50")
        );
    }

    @Nested
    class CreateTests {

        @Test
        @DisplayName("should create and save supplier purchase history entity")
        void shouldCreateAndSaveEntity() {
            Manufacturer manufacturer = buildManufacturer();
            Product product = buildProduct(manufacturer);
            Stock stock = buildStock(product);
            StockRequestDTO dto = buildRequest(product.getId(), manufacturer.getId());
            SupplierPurchaseHistory entity = buildEntity(product, manufacturer, stock);

            when(mapper.toEntity(dto, product, manufacturer, stock)).thenReturn(entity);
            when(repository.save(entity)).thenReturn(entity);

            SupplierPurchaseHistory result = service.create(dto, product, manufacturer, stock);

            assertThat(result).isNotNull();
            assertThat(result.getBatchNumber()).isEqualTo("LT-2026-001");
            verify(repository).save(entity);
        }
    }

    @Nested
    class FindByIdTests {

        @Test
        @DisplayName("should return supplier purchase history when id exists")
        void shouldReturnEntityWhenIdExists() {
            Manufacturer manufacturer = buildManufacturer();
            Product product = buildProduct(manufacturer);
            Stock stock = buildStock(product);
            SupplierPurchaseHistory entity = buildEntity(product, manufacturer, stock);
            SupplierPurchaseHistoryResponseDTO response = buildResponse(entity);

            when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
            when(mapper.toResponse(entity)).thenReturn(response);

            SupplierPurchaseHistoryResponseDTO result = service.findById(entity.getId());

            assertThat(result).isNotNull();
            assertThat(result.batchNumber()).isEqualTo("LT-2026-001");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when id does not exist")
        void shouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
            UUID id = UUID.randomUUID();
            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Supplier Purchase History not found");
        }
    }

    @Nested
    class FindByProductTests {

        @Test
        @DisplayName("should return purchase history for given product")
        void shouldReturnHistoryForGivenProduct() {
            Manufacturer manufacturer = buildManufacturer();
            Product product = buildProduct(manufacturer);
            Stock stock = buildStock(product);
            SupplierPurchaseHistory entity = buildEntity(product, manufacturer, stock);
            SupplierPurchaseHistoryResponseDTO response = buildResponse(entity);

            when(repository.findByProductId(product.getId())).thenReturn(List.of(entity));
            when(mapper.toResponse(entity)).thenReturn(response);

            List<SupplierPurchaseHistoryResponseDTO> result = service.findByProduct(product.getId());

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should return empty list when product has no purchase history")
        void shouldReturnEmptyListWhenProductHasNoHistory() {
            UUID productId = UUID.randomUUID();
            when(repository.findByProductId(productId)).thenReturn(List.of());

            List<SupplierPurchaseHistoryResponseDTO> result = service.findByProduct(productId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class FindByManufacturerTests {

        @Test
        @DisplayName("should return purchase history for given manufacturer")
        void shouldReturnHistoryForGivenManufacturer() {
            Manufacturer manufacturer = buildManufacturer();
            Product product = buildProduct(manufacturer);
            Stock stock = buildStock(product);
            SupplierPurchaseHistory entity = buildEntity(product, manufacturer, stock);
            SupplierPurchaseHistoryResponseDTO response = buildResponse(entity);

            when(repository.findByManufacturerId(manufacturer.getId())).thenReturn(List.of(entity));
            when(mapper.toResponse(entity)).thenReturn(response);

            List<SupplierPurchaseHistoryResponseDTO> result = service.findByManufacturer(manufacturer.getId());

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should return empty list when manufacturer has no purchase history")
        void shouldReturnEmptyListWhenManufacturerHasNoHistory() {
            UUID manufacturerId = UUID.randomUUID();
            when(repository.findByManufacturerId(manufacturerId)).thenReturn(List.of());

            List<SupplierPurchaseHistoryResponseDTO> result = service.findByManufacturer(manufacturerId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class FindByManufacturerPaginatedTests {

        @Test
        @DisplayName("should return paginated purchase history for given manufacturer")
        void shouldReturnPaginatedHistoryForGivenManufacturer() {
            Manufacturer manufacturer = buildManufacturer();
            Product product = buildProduct(manufacturer);
            Stock stock = buildStock(product);
            SupplierPurchaseHistory entity = buildEntity(product, manufacturer, stock);
            SupplierPurchaseHistoryResponseDTO response = buildResponse(entity);
            Pageable pageable = PageRequest.of(0, 10);
            Page<SupplierPurchaseHistory> page = new PageImpl<>(List.of(entity), pageable, 1);

            when(repository.findByManufacturerIdOrderByPurchaseDateDesc(manufacturer.getId(), pageable))
                    .thenReturn(page);
            when(mapper.toResponse(entity)).thenReturn(response);

            Page<SupplierPurchaseHistoryResponseDTO> result =
                    service.findByManufacturerPaginated(manufacturer.getId(), pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("should return empty page when manufacturer has no purchase history")
        void shouldReturnEmptyPageWhenManufacturerHasNoHistory() {
            UUID manufacturerId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 10);
            Page<SupplierPurchaseHistory> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(repository.findByManufacturerIdOrderByPurchaseDateDesc(manufacturerId, pageable))
                    .thenReturn(emptyPage);

            Page<SupplierPurchaseHistoryResponseDTO> result =
                    service.findByManufacturerPaginated(manufacturerId, pageable);

            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    class FindByPeriodTests {

        @Test
        @DisplayName("should return purchase history within given period")
        void shouldReturnHistoryWithinGivenPeriod() {
            Manufacturer manufacturer = buildManufacturer();
            Product product = buildProduct(manufacturer);
            Stock stock = buildStock(product);
            SupplierPurchaseHistory entity = buildEntity(product, manufacturer, stock);
            SupplierPurchaseHistoryResponseDTO response = buildResponse(entity);
            LocalDate start = LocalDate.of(2026, 1, 1);
            LocalDate end = LocalDate.of(2026, 12, 31);

            when(repository.findByPurchaseDateBetween(start, end)).thenReturn(List.of(entity));
            when(mapper.toResponse(entity)).thenReturn(response);

            List<SupplierPurchaseHistoryResponseDTO> result = service.findByPeriod(start, end);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should return empty list when no purchase history exists in given period")
        void shouldReturnEmptyListWhenNoHistoryInPeriod() {
            LocalDate start = LocalDate.of(2026, 1, 1);
            LocalDate end = LocalDate.of(2026, 12, 31);
            when(repository.findByPurchaseDateBetween(start, end)).thenReturn(List.of());

            List<SupplierPurchaseHistoryResponseDTO> result = service.findByPeriod(start, end);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class GetAveragePurchasePriceByProductTests {

        @Test
        @DisplayName("should return average purchase price for product in given period")
        void shouldReturnAveragePurchasePrice() {
            UUID productId = UUID.randomUUID();
            LocalDate start = LocalDate.of(2026, 1, 1);
            LocalDate end = LocalDate.of(2026, 12, 31);

            when(repository.findAverageUnitCostByProduct(productId, start, end))
                    .thenReturn(Optional.of(new BigDecimal("12.75")));

            BigDecimal result = service.getAveragePurchasePriceByProduct(productId, start, end);

            assertThat(result).isEqualByComparingTo("12.75");
        }

        @Test
        @DisplayName("should return zero when no purchase history exists for product in given period")
        void shouldReturnZeroWhenNoHistoryExists() {
            UUID productId = UUID.randomUUID();
            LocalDate start = LocalDate.of(2026, 1, 1);
            LocalDate end = LocalDate.of(2026, 12, 31);

            when(repository.findAverageUnitCostByProduct(productId, start, end))
                    .thenReturn(Optional.empty());

            BigDecimal result = service.getAveragePurchasePriceByProduct(productId, start, end);

            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }
}