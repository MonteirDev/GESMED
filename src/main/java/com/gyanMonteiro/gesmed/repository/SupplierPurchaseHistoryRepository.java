package com.gyanMonteiro.gesmed.repository;

import com.gyanMonteiro.gesmed.entity.SupplierPurchaseHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupplierPurchaseHistoryRepository extends JpaRepository<SupplierPurchaseHistory, UUID> {
    List<SupplierPurchaseHistory> findByProductId(UUID productId);
    List<SupplierPurchaseHistory> findByManufacturerId(UUID manufacturerId);
    List<SupplierPurchaseHistory> findByPurchaseDateBetween(LocalDate start, LocalDate end);
    Optional<SupplierPurchaseHistory> findByBatchNumber(String batchNumber);
    Page<SupplierPurchaseHistory> findByManufacturerIdOrderByPurchaseDateDesc(UUID manufacturerId, Pageable pageable);

    //Custo médio de um produto num período (para análise de preço)
    @Query("SELECT AVG(s.unitPrice) FROM SupplierPurchaseHistory s WHERE s.product.id = :productId AND s.purchaseDate BETWEEN :start AND :end")
    Optional<BigDecimal> findAverageUnitCostByProduct(
            @Param("productId") UUID productId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);
}
