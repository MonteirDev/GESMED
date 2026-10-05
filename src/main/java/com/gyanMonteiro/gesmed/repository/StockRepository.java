package com.gyanMonteiro.gesmed.repository;

import com.gyanMonteiro.gesmed.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockRepository extends JpaRepository<Stock, UUID> {
    List<Stock> findByProductId(UUID productId);
    List<Stock> findByProductAndQuantityGreaterThanOrderByExpiryDateAsc(UUID productId, int quantity);
    List<Stock> findByExpiryDateBefore(String date);

    // Soma quantidade disponível (quantidade total - quantidade reservada) de todos os lotes de um produto
    @Query("SELECT SUM(s.quantity - s.reserveQuantity) FROM Stock s WHERE s.product.id = :productId")
    Optional<Integer> findAvailableQuantityByProductId(UUID productId);

    // Lista os lotes com saldo disponível de um produto, ordenados do que vence primeiro para o que vence por último (FEFO)
    @Query("SELECT s FROM Stock s WHERE s.product.id = :productId AND (s.quantity - s.reserveQuantity) > 0 ORDER BY s.expiryDate ASC")
    List<Stock> findAvailableStockByProductOrderByExpiry(UUID productId);
}
