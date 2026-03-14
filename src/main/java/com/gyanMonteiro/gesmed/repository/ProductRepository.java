package com.gyanMonteiro.gesmed.repository;
import com.gyanMonteiro.gesmed.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByNameIgnoreCase(String name);
    List<Product> findByManufacturerNameIgnoreCase(String name);
}
