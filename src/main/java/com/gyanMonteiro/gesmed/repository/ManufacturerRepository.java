package com.gyanMonteiro.gesmed.repository;

import com.gyanMonteiro.gesmed.entity.Manufacturer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ManufacturerRepository extends JpaRepository<Manufacturer, UUID> {
    List<Manufacturer> findByNameIgnoreCase(String name);
    List<Manufacturer> findByCnpj(String cnpj);
}
