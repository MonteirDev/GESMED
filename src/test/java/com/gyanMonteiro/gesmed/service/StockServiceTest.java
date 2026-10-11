package com.gyanMonteiro.gesmed.service;

import com.gyanMonteiro.gesmed.dto.request.StockRequestDTO;
import com.gyanMonteiro.gesmed.dto.response.StockOperationResultDTO;
import com.gyanMonteiro.gesmed.dto.response.StockResponseDTO;
import com.gyanMonteiro.gesmed.entity.Manufacturer;
import com.gyanMonteiro.gesmed.entity.Product;
import com.gyanMonteiro.gesmed.entity.Stock;
import com.gyanMonteiro.gesmed.exceptions.BusinessException;
import com.gyanMonteiro.gesmed.exceptions.ResourceNotFoundException;
import com.gyanMonteiro.gesmed.mapper.StockMapper;
import com.gyanMonteiro.gesmed.repository.ProductRepository;
import com.gyanMonteiro.gesmed.repository.StockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockRepository repository;

    @Mock
    private StockMapper mapper;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SupplierPurchaseHistoryService supplierPurchaseHistoryService;

    @InjectMocks
    private StockService service;

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

    private Stock buildStock(Product product, String batchNumber, LocalDate expiryDate, Integer quantity, Integer reserveQuantity) {
        Stock stock = new Stock();
        stock.setId(UUID.randomUUID());
        stock.setBatchNumber(batchNumber);
        stock.setExpiryDate(expiryDate);
        stock.setQuantity(quantity);
        stock.setReserveQuantity(reserveQuantity);
        stock.setProduct(product);
        return stock;
    }

    private StockRequestDTO buildRequest(UUID productId) {
        return new StockRequestDTO(
                productId, null, "LT-2026-001",
                LocalDate.of(2026, 12, 15), LocalDate.of(2026, 9, 1),
                100, new BigDecimal("12.50")
        );
    }

    private StockResponseDTO buildResponse(Stock stock) {
        return new StockResponseDTO(
                stock.getId(), null, stock.getBatchNumber(), stock.getExpiryDate(),
                stock.getQuantity(), stock.getReserveQuantity(), stock.getCreatedAt()
        );
    }

    @Nested
    class CreateEntryTests {

        @Test
        @DisplayName("should create stock and generate supplier purchase history")
        void shouldCreateStockAndGenerateSupplierPurchaseHistory() {
            Manufacturer manufacturer = buildManufacturer();
            Product product = buildProduct(manufacturer);
            StockRequestDTO dto = buildRequest(product.getId());
            Stock stock = buildStock(product, "LT-2026-001", LocalDate.of(2026, 12, 15), 100, 0);
            StockResponseDTO response = buildResponse(stock);

            when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
            when(mapper.toEntity(dto, product)).thenReturn(stock);
            when(repository.save(stock)).thenReturn(stock);
            when(mapper.toResponse(stock)).thenReturn(response);

            StockResponseDTO result = service.createEntry(dto);

            assertThat(result).isNotNull();
            assertThat(result.batchNumber()).isEqualTo("LT-2026-001");
            verify(repository).save(stock);
            verify(supplierPurchaseHistoryService).create(dto, product, manufacturer, stock);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when product not found")
        void shouldThrowResourceNotFoundExceptionWhenProductNotFound() {
            UUID productId = UUID.randomUUID();
            StockRequestDTO dto = buildRequest(productId);

            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createEntry(dto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Product not found");

            verifyNoInteractions(supplierPurchaseHistoryService);
        }

        @Test
        @DisplayName("should throw BusinessException when batch number already exists")
        void shouldThrowBusinessExceptionWhenBatchNumberAlreadyExists() {
            Manufacturer manufacturer = buildManufacturer();
            Product product = buildProduct(manufacturer);
            StockRequestDTO dto = buildRequest(product.getId());

            when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
            when(repository.existsByBatchNumber(dto.batchNumber())).thenReturn(true);

            assertThatThrownBy(() -> service.createEntry(dto))
                    .isInstanceOf(BusinessException.class);

            verify(repository, never()).save(any());
            verifyNoInteractions(supplierPurchaseHistoryService);
        }
    }

    @Nested
    class FindByIdTests {

        @Test
        @DisplayName("should return stock when id exists")
        void shouldReturnStockWhenIdExists() {
            Manufacturer manufacturer = buildManufacturer();
            Product product = buildProduct(manufacturer);
            Stock stock = buildStock(product, "LT-2026-001", LocalDate.of(2026, 12, 15), 100, 0);
            StockResponseDTO response = buildResponse(stock);

            when(repository.findById(stock.getId())).thenReturn(Optional.of(stock));
            when(mapper.toResponse(stock)).thenReturn(response);

            StockResponseDTO result = service.findById(stock.getId());

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
                    .hasMessage("Stock not found");
        }
    }

    @Nested
    class FindByProductTests {

        @Test
        @DisplayName("should return batches for given product")
        void shouldReturnBatchesForGivenProduct() {
            Manufacturer manufacturer = buildManufacturer();
            Product product = buildProduct(manufacturer);
            Stock stock = buildStock(product, "LT-2026-001", LocalDate.of(2026, 12, 15), 100, 0);
            StockResponseDTO response = buildResponse(stock);

            when(repository.findByProductId(product.getId())).thenReturn(List.of(stock));
            when(mapper.toResponse(stock)).thenReturn(response);

            List<StockResponseDTO> result = service.findByProduct(product.getId());

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should return empty list when product has no batches")
        void shouldReturnEmptyListWhenProductHasNoBatches() {
            UUID productId = UUID.randomUUID();
            when(repository.findByProductId(productId)).thenReturn(List.of());

            List<StockResponseDTO> result = service.findByProduct(productId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class GetAvailableQuantityTests {

        @Test
        @DisplayName("should return available quantity for product")
        void shouldReturnAvailableQuantityForProduct() {
            UUID productId = UUID.randomUUID();
            when(repository.findAvailableQuantityByProductId(productId)).thenReturn(Optional.of(450));

            Integer result = service.getAvailableQuantity(productId);

            assertThat(result).isEqualTo(450);
        }

        @Test
        @DisplayName("should return zero when product has no stock")
        void shouldReturnZeroWhenProductHasNoStock() {
            UUID productId = UUID.randomUUID();
            when(repository.findAvailableQuantityByProductId(productId)).thenReturn(Optional.empty());

            Integer result = service.getAvailableQuantity(productId);

            assertThat(result).isZero();
        }
    }

    @Nested
    class FindAvailableByProductFEFOTests {

        @Test
        @DisplayName("should return available batches ordered by expiry date")
        void shouldReturnAvailableBatchesOrderedByExpiryDate() {
            Manufacturer manufacturer = buildManufacturer();
            Product product = buildProduct(manufacturer);
            Stock stock = buildStock(product, "LT-2026-001", LocalDate.of(2026, 12, 15), 100, 0);
            StockResponseDTO response = buildResponse(stock);

            when(repository.findAvailableStockByProductOrderByExpiry(product.getId())).thenReturn(List.of(stock));
            when(mapper.toResponse(stock)).thenReturn(response);

            List<StockResponseDTO> result = service.findAvailableByProductFEFO(product.getId());

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should return empty list when no batches are available")
        void shouldReturnEmptyListWhenNoBatchesAreAvailable() {
            UUID productId = UUID.randomUUID();
            when(repository.findAvailableStockByProductOrderByExpiry(productId)).thenReturn(List.of());

            List<StockResponseDTO> result = service.findAvailableByProductFEFO(productId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class FindExpiringSoonTests {

        @Test
        @DisplayName("should return batches expiring before given date")
        void shouldReturnBatchesExpiringBeforeGivenDate() {
            Manufacturer manufacturer = buildManufacturer();
            Product product = buildProduct(manufacturer);
            Stock stock = buildStock(product, "LT-2026-001", LocalDate.of(2026, 10, 15), 100, 0);
            StockResponseDTO response = buildResponse(stock);
            LocalDate limitDate = LocalDate.of(2026, 11, 1);

            when(repository.findByExpiryDateBefore(limitDate)).thenReturn(List.of(stock));
            when(mapper.toResponse(stock)).thenReturn(response);

            List<StockResponseDTO> result = service.findExpiringSoon(limitDate);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should return empty list when no batches are expiring")
        void shouldReturnEmptyListWhenNoBatchesAreExpiring() {
            LocalDate limitDate = LocalDate.of(2026, 11, 1);
            when(repository.findByExpiryDateBefore(limitDate)).thenReturn(List.of());

            List<StockResponseDTO> result = service.findExpiringSoon(limitDate);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class ReserveQuantityTests {

        @Test
        @DisplayName("should reserve quantity from a single batch when enough available")
        void shouldReserveFromSingleBatchWhenEnoughAvailable() {
            Manufacturer manufacturer = buildManufacturer();
            Product product = buildProduct(manufacturer);
            Stock batch = buildStock(product, "LT-2026-001", LocalDate.of(2026, 12, 15), 100, 0);

            when(repository.findAvailableStockByProductOrderByExpiry(product.getId())).thenReturn(List.of(batch));
            when(repository.findAvailableQuantityByProductId(product.getId())).thenReturn(Optional.of(40));

            StockOperationResultDTO result = service.reserveQuantity(product.getId(), 60);

            assertThat(batch.getReserveQuantity()).isEqualTo(60);
            assertThat(result.quantityAffected()).isEqualTo(60);
            assertThat(result.remainingQuantity()).isEqualTo(40);
            verify(repository).save(batch);
        }

        @Test
        @DisplayName("should reserve quantity across multiple batches following FEFO")
        void shouldReserveAcrossMultipleBatchesFollowingFEFO() {
            Manufacturer manufacturer = buildManufacturer();
            Product product = buildProduct(manufacturer);
            Stock earlierBatch = buildStock(product, "LT-2026-001", LocalDate.of(2026, 12, 15), 30, 0);
            Stock laterBatch = buildStock(product, "LT-2026-002", LocalDate.of(2027, 3, 20), 100, 0);

            when(repository.findAvailableStockByProductOrderByExpiry(product.getId()))
                    .thenReturn(List.of(earlierBatch, laterBatch));
            when(repository.findAvailableQuantityByProductId(product.getId())).thenReturn(Optional.of(60));

            StockOperationResultDTO result = service.reserveQuantity(product.getId(), 60);

            assertThat(earlierBatch.getReserveQuantity()).isEqualTo(30);
            assertThat(laterBatch.getReserveQuantity()).isEqualTo(30);
            assertThat(result.quantityAffected()).isEqualTo(60);
            verify(repository).save(earlierBatch);
            verify(repository).save(laterBatch);
        }

        @Test
        @DisplayName("should throw BusinessException when not enough stock to reserve")
        void shouldThrowBusinessExceptionWhenNotEnoughStockToReserve() {
            Manufacturer manufacturer = buildManufacturer();
            Product product = buildProduct(manufacturer);
            Stock batch = buildStock(product, "LT-2026-001", LocalDate.of(2026, 12, 15), 30, 0);

            when(repository.findAvailableStockByProductOrderByExpiry(product.getId())).thenReturn(List.of(batch));

            assertThatThrownBy(() -> service.reserveQuantity(product.getId(), 60))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    class ReleaseReservedQuantityTests {

        @Test
        @DisplayName("should release reserved quantity from batches")
        void shouldReleaseReservedQuantityFromBatches() {
            Manufacturer manufacturer = buildManufacturer();
            Product product = buildProduct(manufacturer);
            Stock batch = buildStock(product, "LT-2026-001", LocalDate.of(2026, 12, 15), 100, 60);

            when(repository.findByProductId(product.getId())).thenReturn(List.of(batch));
            when(repository.findAvailableQuantityByProductId(product.getId())).thenReturn(Optional.of(100));

            StockOperationResultDTO result = service.releaseReservedQuantity(product.getId(), 60);

            assertThat(batch.getReserveQuantity()).isZero();
            assertThat(result.quantityAffected()).isEqualTo(60);
            verify(repository).save(batch);
        }

        @Test
        @DisplayName("should release across multiple batches with reserved quantity")
        void shouldReleaseAcrossMultipleBatches() {
            Manufacturer manufacturer = buildManufacturer();
            Product product = buildProduct(manufacturer);
            Stock batchOne = buildStock(product, "LT-2026-001", LocalDate.of(2026, 12, 15), 100, 20);
            Stock batchTwo = buildStock(product, "LT-2026-002", LocalDate.of(2027, 3, 20), 100, 40);

            when(repository.findByProductId(product.getId())).thenReturn(List.of(batchOne, batchTwo));
            when(repository.findAvailableQuantityByProductId(product.getId())).thenReturn(Optional.of(200));

            StockOperationResultDTO result = service.releaseReservedQuantity(product.getId(), 60);

            assertThat(batchOne.getReserveQuantity()).isZero();
            assertThat(batchTwo.getReserveQuantity()).isZero();
            assertThat(result.quantityAffected()).isEqualTo(60);
        }

        @Test
        @DisplayName("should throw BusinessException when not enough reserved stock to release")
        void shouldThrowBusinessExceptionWhenNotEnoughReservedStockToRelease() {
            Manufacturer manufacturer = buildManufacturer();
            Product product = buildProduct(manufacturer);
            Stock batch = buildStock(product, "LT-2026-001", LocalDate.of(2026, 12, 15), 100, 20);

            when(repository.findByProductId(product.getId())).thenReturn(List.of(batch));

            assertThatThrownBy(() -> service.releaseReservedQuantity(product.getId(), 60))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("should throw BusinessException when no batches have reserved quantity")
        void shouldThrowBusinessExceptionWhenNoBatchesHaveReservedQuantity() {
            Manufacturer manufacturer = buildManufacturer();
            Product product = buildProduct(manufacturer);
            Stock batch = buildStock(product, "LT-2026-001", LocalDate.of(2026, 12, 15), 100, 0);

            when(repository.findByProductId(product.getId())).thenReturn(List.of(batch));

            assertThatThrownBy(() -> service.releaseReservedQuantity(product.getId(), 10))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    class DispatchStockTests {

        @Test
        @DisplayName("should dispatch stock respecting reserved quantity")
        void shouldDispatchStockRespectingReservedQuantity() {
            Manufacturer manufacturer = buildManufacturer();
            Product product = buildProduct(manufacturer);
            Stock batch = buildStock(product, "LT-2026-001", LocalDate.of(2026, 12, 15), 100, 60);

            when(repository.findByProductIdAndQuantityGreaterThanOrderByExpiryDateAsc(product.getId(), 0))
                    .thenReturn(List.of(batch));
            when(repository.findAvailableQuantityByProductId(product.getId())).thenReturn(Optional.of(40));

            StockOperationResultDTO result = service.dispatchStock(product.getId(), 60);

            assertThat(batch.getQuantity()).isEqualTo(40);
            assertThat(batch.getReserveQuantity()).isZero();
            assertThat(result.quantityAffected()).isEqualTo(60);
            verify(repository).save(batch);
        }

        @Test
        @DisplayName("should skip batches without reserved quantity")
        void shouldSkipBatchesWithoutReservedQuantity() {
            Manufacturer manufacturer = buildManufacturer();
            Product product = buildProduct(manufacturer);
            Stock batchWithoutReserve = buildStock(product, "LT-2026-001", LocalDate.of(2026, 12, 15), 100, 0);
            Stock batchWithReserve = buildStock(product, "LT-2026-002", LocalDate.of(2027, 3, 20), 100, 30);

            when(repository.findByProductIdAndQuantityGreaterThanOrderByExpiryDateAsc(product.getId(), 0))
                    .thenReturn(List.of(batchWithoutReserve, batchWithReserve));
            when(repository.findAvailableQuantityByProductId(product.getId())).thenReturn(Optional.of(170));

            StockOperationResultDTO result = service.dispatchStock(product.getId(), 30);

            assertThat(batchWithoutReserve.getQuantity()).isEqualTo(100);
            assertThat(batchWithReserve.getQuantity()).isEqualTo(70);
            assertThat(batchWithReserve.getReserveQuantity()).isZero();
            verify(repository, never()).save(batchWithoutReserve);
            verify(repository).save(batchWithReserve);
        }

        @Test
        @DisplayName("should throw BusinessException when dispatch exceeds reserved quantity")
        void shouldThrowBusinessExceptionWhenDispatchExceedsReservedQuantity() {
            Manufacturer manufacturer = buildManufacturer();
            Product product = buildProduct(manufacturer);
            Stock batch = buildStock(product, "LT-2026-001", LocalDate.of(2026, 12, 15), 100, 20);

            when(repository.findByProductIdAndQuantityGreaterThanOrderByExpiryDateAsc(product.getId(), 0))
                    .thenReturn(List.of(batch));

            assertThatThrownBy(() -> service.dispatchStock(product.getId(), 60))
                    .isInstanceOf(BusinessException.class);
        }
    }
}