package com.gyanMonteiro.gesmed.repository;

import com.gyanMonteiro.gesmed.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
    Optional<Client> findByNameIgnoreCase(String name);
    Optional<Client> findByCnpj(String cnpj);
}
