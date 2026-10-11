package com.gyanMonteiro.gesmed.repository;

import com.gyanMonteiro.gesmed.entity.Manufacturer;
import com.gyanMonteiro.gesmed.entity.Product;
import com.gyanMonteiro.gesmed.entity.Stock;
import com.gyanMonteiro.gesmed.entity.SupplierPurchaseHistory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SupplierPurchaseHistoryRepositoryTest {

    @Autowired
    EntityManager entityManager;

    @Autowired
    SupplierPurchaseHistoryRepository repository;

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

    private Stock createStock(String batchNumber, Product product) {
        Stock stock = new Stock();
        stock.setBatchNumber(batchNumber);
        stock.setExpiryDate(LocalDate.of(2027, 12, 31));
        stock.setQuantity(100);
        stock.setReserveQuantity(0);
        stock.setProduct(product);
        stock.setCreatedAt(LocalDateTime.now());
        entityManager.persist(stock);
        return stock;
    }

    private SupplierPurchaseHistory createHistory(String batchNumber, String unitPrice, LocalDate purchaseDate,
                                                  Product product, Manufacturer manufacturer) {
        Stock stock = createStock(batchNumber, product);

        SupplierPurchaseHistory history = new SupplierPurchaseHistory();
        history.setBatchNumber(batchNumber);
        history.setQuantity(100);
        history.setUnitPrice(new BigDecimal(unitPrice));
        history.setPurchaseDate(purchaseDate);
        history.setProduct(product);
        history.setManufacturer(manufacturer);
        history.setStock(stock);
        history.setCreatedAt(LocalDateTime.now());
        entityManager.persist(history);
        return history;
    }

    @Test
    @DisplayName("Should return SupplierPurchaseHistory when ID exists")
    void findByIdShouldReturnHistory() {
        Manufacturer manufacturer = createManufacturer("Laboratório Exemplo S.A.", "12.345.678/0001-99");
        Product product = createProduct("Dipirona 500mg", "DIP-12345", manufacturer);
        SupplierPurchaseHistory created = createHistory("LT-2026-001", "12.50", LocalDate.of(2026, 9, 1), product, manufacturer);

        Optional<SupplierPurchaseHistory> result = repository.findById(created.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getBatchNumber()).isEqualTo("LT-2026-001");
    }

    @Test
    @DisplayName("Should return empty Optional when ID does not exist")
    void findByIdShouldReturnEmpty() {
        Optional<SupplierPurchaseHistory> result = repository.findById(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return purchase history by product id")
    void findByProductIdShouldReturnHistory() {
        Manufacturer manufacturer = createManufacturer("Laboratório Exemplo S.A.", "12.345.678/0001-99");
        Product product = createProduct("Dipirona 500mg", "DIP-12345", manufacturer);
        Product otherProduct = createProduct("Amoxicilina 500mg", "AMX-12345", manufacturer);
        createHistory("LT-2026-001", "12.50", LocalDate.of(2026, 9, 1), product, manufacturer);
        createHistory("LT-2026-002", "12.80", LocalDate.of(2026, 9, 10), product, manufacturer);
        createHistory("LT-2026-003", "30.00", LocalDate.of(2026, 9, 15), otherProduct, manufacturer);

        List<SupplierPurchaseHistory> result = repository.findByProductId(product.getId());

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Should return empty list when product has no purchase history")
    void findByProductIdShouldReturnEmptyList() {
        List<SupplierPurchaseHistory> result = repository.findByProductId(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return purchase history by manufacturer id")
    void findByManufacturerIdShouldReturnHistory() {
        Manufacturer manufacturer = createManufacturer("Laboratório Exemplo S.A.", "12.345.678/0001-99");
        Manufacturer otherManufacturer = createManufacturer("Outro Laboratório", "98.765.432/0001-11");
        Product product = createProduct("Dipirona 500mg", "DIP-12345", manufacturer);
        createHistory("LT-2026-001", "12.50", LocalDate.of(2026, 9, 1), product, manufacturer);
        createHistory("LT-2026-002", "12.80", LocalDate.of(2026, 9, 10), product, manufacturer);
        createHistory("LT-2026-003", "13.00", LocalDate.of(2026, 9, 15), product, otherManufacturer);

        List<SupplierPurchaseHistory> result = repository.findByManufacturerId(manufacturer.getId());

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Should return empty list when manufacturer has no purchase history")
    void findByManufacturerIdShouldReturnEmptyList() {
        List<SupplierPurchaseHistory> result = repository.findByManufacturerId(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return purchase history within period, including boundary dates")
    void findByPurchaseDateBetweenShouldReturnHistoryWithinPeriod() {
        Manufacturer manufacturer = createManufacturer("Laboratório Exemplo S.A.", "12.345.678/0001-99");
        Product product = createProduct("Dipirona 500mg", "DIP-12345", manufacturer);
        createHistory("LT-2026-001", "12.50", LocalDate.of(2026, 9, 1), product, manufacturer);
        createHistory("LT-2026-002", "12.80", LocalDate.of(2026, 9, 30), product, manufacturer);
        createHistory("LT-2026-003", "13.00", LocalDate.of(2026, 10, 15), product, manufacturer);

        List<SupplierPurchaseHistory> result =
                repository.findByPurchaseDateBetween(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30));

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Should return empty list when no purchase history exists in period")
    void findByPurchaseDateBetweenShouldReturnEmptyList() {
        Manufacturer manufacturer = createManufacturer("Laboratório Exemplo S.A.", "12.345.678/0001-99");
        Product product = createProduct("Dipirona 500mg", "DIP-12345", manufacturer);
        createHistory("LT-2026-001", "12.50", LocalDate.of(2026, 9, 1), product, manufacturer);

        List<SupplierPurchaseHistory> result =
                repository.findByPurchaseDateBetween(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return purchase history by batch number")
    void findByBatchNumberShouldReturnHistory() {
        Manufacturer manufacturer = createManufacturer("Laboratório Exemplo S.A.", "12.345.678/0001-99");
        Product product = createProduct("Dipirona 500mg", "DIP-12345", manufacturer);
        createHistory("LT-2026-001", "12.50", LocalDate.of(2026, 9, 1), product, manufacturer);

        Optional<SupplierPurchaseHistory> result = repository.findByBatchNumber("LT-2026-001");

        assertThat(result).isPresent();
        assertThat(result.get().getBatchNumber()).isEqualTo("LT-2026-001");
    }

    @Test
    @DisplayName("Should return empty Optional when batch number does not exist")
    void findByBatchNumberShouldReturnEmpty() {
        Optional<SupplierPurchaseHistory> result = repository.findByBatchNumber("LT-2026-999");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return purchase history by manufacturer ordered by purchase date descending")
    void findByManufacturerIdOrderByPurchaseDateDescShouldReturnOrderedPage() {
        Manufacturer manufacturer = createManufacturer("Laboratório Exemplo S.A.", "12.345.678/0001-99");
        Product product = createProduct("Dipirona 500mg", "DIP-12345", manufacturer);
        createHistory("LT-2026-001", "12.50", LocalDate.of(2026, 9, 1), product, manufacturer);
        createHistory("LT-2026-003", "13.00", LocalDate.of(2026, 9, 20), product, manufacturer);
        createHistory("LT-2026-002", "12.80", LocalDate.of(2026, 9, 10), product, manufacturer);

        Page<SupplierPurchaseHistory> result = repository
                .findByManufacturerIdOrderByPurchaseDateDesc(manufacturer.getId(), PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent().get(0).getBatchNumber()).isEqualTo("LT-2026-003");
        assertThat(result.getContent().get(1).getBatchNumber()).isEqualTo("LT-2026-002");
        assertThat(result.getContent().get(2).getBatchNumber()).isEqualTo("LT-2026-001");
    }

    @Test
    @DisplayName("Should respect page size when returning paginated purchase history")
    void findByManufacturerIdOrderByPurchaseDateDescShouldRespectPageSize() {
        Manufacturer manufacturer = createManufacturer("Laboratório Exemplo S.A.", "12.345.678/0001-99");
        Product product = createProduct("Dipirona 500mg", "DIP-12345", manufacturer);
        createHistory("LT-2026-001", "12.50", LocalDate.of(2026, 9, 1), product, manufacturer);
        createHistory("LT-2026-002", "12.80", LocalDate.of(2026, 9, 10), product, manufacturer);
        createHistory("LT-2026-003", "13.00", LocalDate.of(2026, 9, 20), product, manufacturer);

        Page<SupplierPurchaseHistory> result = repository
                .findByManufacturerIdOrderByPurchaseDateDesc(manufacturer.getId(), PageRequest.of(0, 2));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should return average unit price of a product within period")
    void findAverageUnitCostByProductShouldReturnAverage() {
        Manufacturer manufacturer = createManufacturer("Laboratório Exemplo S.A.", "12.345.678/0001-99");
        Product product = createProduct("Dipirona 500mg", "DIP-12345", manufacturer);
        Product otherProduct = createProduct("Amoxicilina 500mg", "AMX-12345", manufacturer);
        createHistory("LT-2026-001", "10.00", LocalDate.of(2026, 9, 1), product, manufacturer);
        createHistory("LT-2026-002", "20.00", LocalDate.of(2026, 9, 10), product, manufacturer);
        createHistory("LT-2026-003", "90.00", LocalDate.of(2026, 12, 1), product, manufacturer);      // fora do período
        createHistory("LT-2026-004", "500.00", LocalDate.of(2026, 9, 5), otherProduct, manufacturer); // outro produto

        Optional<BigDecimal> result = repository.findAverageUnitCostByProduct(
                product.getId(), LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30));

        // (10.00 + 20.00) / 2 = 15.00
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualByComparingTo("15.00");
    }

    @Test
    @DisplayName("Should return empty Optional when product has no purchase history in period")
    void findAverageUnitCostByProductShouldReturnEmptyWhenNoHistory() {
        Optional<BigDecimal> result = repository.findAverageUnitCostByProduct(
                UUID.randomUUID(), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertThat(result).isEmpty();
    }
}