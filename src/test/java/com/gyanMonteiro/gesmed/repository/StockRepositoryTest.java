package com.gyanMonteiro.gesmed.repository;

import com.gyanMonteiro.gesmed.entity.Manufacturer;
import com.gyanMonteiro.gesmed.entity.Product;
import com.gyanMonteiro.gesmed.entity.Stock;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class StockRepositoryTest {

    @Autowired
    EntityManager entityManager;

    @Autowired
    StockRepository stockRepository;

    private Manufacturer createManufacturer(String name, String cnpj) {
        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setName(name);
        manufacturer.setCnpj(cnpj);
        manufacturer.setActive(true);
        manufacturer.setCreatedAt(LocalDateTime.now());
        entityManager.persist(manufacturer);
        return manufacturer;
    }

    private Product createProduct(String name, String sku, Manufacturer manufacturer) {
        Product product = new Product();
        product.setName(name);
        product.setSku(sku);
        product.setDosage("500mg");
        product.setUnitofMeasure("CAIXA");
        product.setManufacturer(manufacturer);
        product.setCreatedAt(LocalDateTime.now());
        entityManager.persist(product);
        return product;
    }

    private Stock createStock(String batchNumber, LocalDate expiryDate, Integer quantity, Integer reserveQuantity, Product product) {
        Stock stock = new Stock();
        stock.setBatchNumber(batchNumber);
        stock.setExpiryDate(expiryDate);
        stock.setQuantity(quantity);
        stock.setReserveQuantity(reserveQuantity);
        stock.setProduct(product);
        stock.setCreatedAt(LocalDateTime.now());
        entityManager.persist(stock);
        return stock;
    }

    @Test
    @DisplayName("Should return Stock when ID exists")
    void findByIdShouldReturnStock() {
        Manufacturer manufacturer = createManufacturer("Laboratório Exemplo S.A.", "1234123456789");
        Product product = createProduct("Dipirona 500mg", "DIP-12345", manufacturer);
        Stock created = createStock("LT-2026-001", LocalDate.of(2026, 12, 15), 100, 0, product);

        Optional<Stock> result = stockRepository.findById(created.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getBatchNumber()).isEqualTo("LT-2026-001");
    }

    @Test
    @DisplayName("Should return empty Optional when ID does not exist")
    void findByIdShouldReturnEmpty() {
        Optional<Stock> result = stockRepository.findById(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return all Stock batches by product id")
    void findByProductIdShouldReturnBatches() {
        Manufacturer manufacturer = createManufacturer("Laboratório Exemplo S.A.", "1234123456789");
        Product product = createProduct("Dipirona 500mg", "DIP-12345", manufacturer);
        createStock("LT-2026-001", LocalDate.of(2026, 12, 15), 100, 0, product);
        createStock("LT-2026-002", LocalDate.of(2027, 3, 20), 200, 0, product);
        createStock("LT-2026-003", LocalDate.of(2027, 8, 30), 150, 0, product);

        List<Stock> result = stockRepository.findByProductId(product.getId());

        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("Should return empty list when product has no stock batches")
    void findByProductIdShouldReturnEmptyListWhenNoBatches() {
        List<Stock> result = stockRepository.findByProductId(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return batches with quantity greater than given value ordered by expiry date")
    void findByProductAndQuantityGreaterThanOrderByExpiryDateAscShouldReturnOrderedBatches() {
        Manufacturer manufacturer = createManufacturer("Laboratório Exemplo S.A.", "1234123456789");
        Product product = createProduct("Dipirona 500mg", "DIP-12345", manufacturer);
        createStock("LT-2026-003", LocalDate.of(2027, 8, 30), 150, 0, product);
        createStock("LT-2026-001", LocalDate.of(2026, 12, 15), 100, 0, product);
        createStock("LT-2026-002", LocalDate.of(2027, 3, 20), 200, 0, product);
        createStock("LT-2026-000", LocalDate.of(2026, 10, 1), 0, 0, product);

        List<Stock> result = stockRepository
                .findByProductIdAndQuantityGreaterThanOrderByExpiryDateAsc(product.getId(), 0);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getBatchNumber()).isEqualTo("LT-2026-001");
        assertThat(result.get(1).getBatchNumber()).isEqualTo("LT-2026-002");
        assertThat(result.get(2).getBatchNumber()).isEqualTo("LT-2026-003");
    }

    @Test
    @DisplayName("Should return batches expiring before given date")
    void findByExpiryDateBeforeShouldReturnExpiringBatches() {
        Manufacturer manufacturer = createManufacturer("Laboratório Exemplo S.A.", "1234123456789");
        Product product = createProduct("Dipirona 500mg", "DIP-12345", manufacturer);
        createStock("LT-2026-001", LocalDate.of(2026, 12, 15), 100, 0, product);
        createStock("LT-2026-002", LocalDate.of(2027, 3, 20), 200, 0, product);

        List<Stock> result = stockRepository.findByExpiryDateBefore(LocalDate.of(2027, 1, 1));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBatchNumber()).isEqualTo("LT-2026-001");
    }

    @Test
    @DisplayName("Should return empty list when no batches expire before given date")
    void findByExpiryDateBeforeShouldReturnEmptyListWhenNoMatch() {
        Manufacturer manufacturer = createManufacturer("Laboratório Exemplo S.A.", "1234123456789");
        Product product = createProduct("Dipirona 500mg", "DIP-12345", manufacturer);
        createStock("LT-2026-001", LocalDate.of(2027, 12, 15), 100, 0, product);

        List<Stock> result = stockRepository.findByExpiryDateBefore(LocalDate.of(2026, 1, 1));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return summed available quantity across all batches of a product")
    void findAvailableQuantityByProductIdShouldReturnSum() {
        Manufacturer manufacturer = createManufacturer("Laboratório Exemplo S.A.", "1234123456789");
        Product product = createProduct("Dipirona 500mg", "DIP-12345", manufacturer);
        createStock("LT-2026-001", LocalDate.of(2026, 12, 15), 100, 20, product);
        createStock("LT-2026-002", LocalDate.of(2027, 3, 20), 200, 50, product);
        createStock("LT-2026-003", LocalDate.of(2027, 8, 30), 150, 0, product);

        Optional<Integer> result = stockRepository.findAvailableQuantityByProductId(product.getId());

        // (100-20) + (200-50) + (150-0) = 380
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(380);
    }

    @Test
    @DisplayName("Should return empty Optional when product has no stock batches for available quantity")
    void findAvailableQuantityByProductIdShouldReturnEmptyWhenNoBatches() {
        Optional<Integer> result = stockRepository.findAvailableQuantityByProductId(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return available batches ordered by expiry date ascending (FEFO)")
    void findAvailableStockByProductOrderByExpiryShouldReturnOrderedBatches() {
        Manufacturer manufacturer = createManufacturer("Laboratório Exemplo S.A.", "1234123456789");
        Product product = createProduct("Dipirona 500mg", "DIP-12345", manufacturer);
        createStock("LT-2026-003", LocalDate.of(2027, 8, 30), 150, 0, product);
        createStock("LT-2026-001", LocalDate.of(2026, 12, 15), 100, 0, product);
        createStock("LT-2026-002", LocalDate.of(2027, 3, 20), 200, 0, product);
        createStock("LT-2026-ZEROED", LocalDate.of(2026, 11, 1), 50, 50, product); // sem saldo disponível

        List<Stock> result = stockRepository.findAvailableStockByProductOrderByExpiry(product.getId());

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getBatchNumber()).isEqualTo("LT-2026-001");
        assertThat(result.get(1).getBatchNumber()).isEqualTo("LT-2026-002");
        assertThat(result.get(2).getBatchNumber()).isEqualTo("LT-2026-003");
    }

    @Test
    @DisplayName("Should return empty list when no batches have available quantity")
    void findAvailableStockByProductOrderByExpiryShouldReturnEmptyWhenNoAvailableBatches() {
        Manufacturer manufacturer = createManufacturer("Laboratório Exemplo S.A.", "1234123456789");
        Product product = createProduct("Dipirona 500mg", "DIP-12345", manufacturer);
        createStock("LT-2026-ZEROED", LocalDate.of(2026, 11, 1), 50, 50, product);

        List<Stock> result = stockRepository.findAvailableStockByProductOrderByExpiry(product.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return true when batch number exists")
    void existsByBatchNumberShouldReturnTrue() {
        Manufacturer manufacturer = createManufacturer("Laboratório Exemplo S.A.", "1234123456789");
        Product product = createProduct("Dipirona 500mg", "DIP-12345", manufacturer);
        createStock("LT-2026-001", LocalDate.of(2026, 12, 15), 100, 0, product);

        assertThat(stockRepository.existsByBatchNumber("LT-2026-001")).isTrue();
    }

    @Test
    @DisplayName("Should return false when batch number does not exist")
    void existsByBatchNumberShouldReturnFalse() {
        assertThat(stockRepository.existsByBatchNumber("LT-2026-999")).isFalse();
    }

    @Test
    @DisplayName("Should not allow two batches with the same batch number")
    void shouldRejectDuplicatedBatchNumber() {
        Manufacturer manufacturer = createManufacturer("Laboratório Exemplo S.A." , "1234123456789");
        Product product = createProduct("Dipirona 500mg", "DIP-12345", manufacturer);
        createStock("LT-2026-001", LocalDate.of(2026, 12, 15), 100, 0, product);
        createStock("LT-2026-001", LocalDate.of(2027, 3, 20), 200, 0, product);

        assertThatThrownBy(() -> entityManager.flush())
                .isInstanceOf(PersistenceException.class);
    }
}