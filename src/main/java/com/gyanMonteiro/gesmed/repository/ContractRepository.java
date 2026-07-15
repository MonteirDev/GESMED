package com.gyanMonteiro.gesmed.repository;

import com.gyanMonteiro.gesmed.entity.Contract;
import com.gyanMonteiro.gesmed.enums.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ContractRepository extends JpaRepository<Contract, UUID> {
    Optional<Contract> findByContractNumber(String contractNumber);
    Optional<Contract> findByClientId(UUID clientId);
    Optional<Contract> findByStatus(ContractStatus status);
}
