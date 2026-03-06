package com.gyanMonteiro.gesmed.repository;

import com.gyanMonteiro.gesmed.entity.Manufacturer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ManufacturerRepository extends JpaRepository<Manufacturer, UUID> {
}
