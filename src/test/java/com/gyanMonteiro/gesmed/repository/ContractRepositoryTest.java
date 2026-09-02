package com.gyanMonteiro.gesmed.repository;

import com.gyanMonteiro.gesmed.entity.Client;
import com.gyanMonteiro.gesmed.entity.Contract;
import com.gyanMonteiro.gesmed.enums.ContractStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ContractRepositoryTest {

    @Autowired
    EntityManager entityManager;

    @Autowired
    ContractRepository contractRepository;

    private Client createClient(String name, String cnpj) {
        Client client = new Client();
        client.setName(name);
        client.setCnpj(cnpj);
        entityManager.persist(client);
        return client;
    }

    private Contract createContract(String contractNumber, ContractStatus status, Client client) {
        Contract contract = new Contract();
        contract.setContractNumber(contractNumber);
        contract.setStartDate(LocalDate.of(2026, 1, 1));
        contract.setEndDate(LocalDate.of(2026, 12, 31));
        contract.setStatus(status);
        contract.setClient(client);
        entityManager.persist(contract);
        return contract;
    }

    @Test
    @DisplayName("Should get Contract by contract number successfully from DB")
    void findByContractNumberSuccess() {
        Client client = createClient("Hospital das Clínicas", "12345678000100");
        createContract("CONT-2026-001", ContractStatus.ACTIVE, client);

        Optional<Contract> result = contractRepository.findByContractNumber("CONT-2026-001");

        assertThat(result).isPresent();
        assertThat(result.get().getContractNumber()).isEqualTo("CONT-2026-001");
    }

    @Test
    @DisplayName("Should not get Contract from DB when contract does not exist")
    void findByContractNumberError() {
        Optional<Contract> result = contractRepository.findByContractNumber("CONT-2026-999");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty Optional when contract number does not match")
    void findByContractNumberShouldReturnEmptyWhenNoMatch() {
        Client client = createClient("Hospital das Clínicas", "12345678000100");
        createContract("CONT-2026-001", ContractStatus.ACTIVE, client);

        Optional<Contract> result = contractRepository.findByContractNumber("CONT-2026-002");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return Contracts by client id")
    void findByClientIdShouldReturnContracts() {
        Client client = createClient("Hospital das Clínicas", "12345678000100");
        createContract("CONT-2026-001", ContractStatus.ACTIVE, client);
        createContract("CONT-2026-002", ContractStatus.SUSPENDED, client);

        List<Contract> result = contractRepository.findByClientId(client.getId());

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Should return empty list when client has no contracts")
    void findByClientIdShouldReturnEmptyListWhenNoContracts() {
        UUID clientId = UUID.randomUUID();

        List<Contract> result = contractRepository.findByClientId(clientId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return Contracts by status")
    void findByStatusShouldReturnContracts() {
        Client client = createClient("Hospital das Clínicas", "12345678000100");
        createContract("CONT-2026-001", ContractStatus.ACTIVE, client);
        createContract("CONT-2026-002", ContractStatus.ACTIVE, client);
        createContract("CONT-2026-003", ContractStatus.CANCELLED, client);

        List<Contract> result = contractRepository.findByStatus(ContractStatus.ACTIVE);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Should return empty list when no contracts exist with given status")
    void findByStatusShouldReturnEmptyListWhenNoMatch() {
        Client client = createClient("Hospital das Clínicas", "12345678000100");
        createContract("CONT-2026-001", ContractStatus.ACTIVE, client);

        List<Contract> result = contractRepository.findByStatus(ContractStatus.EXPIRED);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return Contract when ID exists")
    void findByIdShouldReturnContract() {
        Client client = createClient("Hospital das Clínicas", "12345678000100");
        Contract created = createContract("CONT-2026-001", ContractStatus.ACTIVE, client);
        UUID id = created.getId();

        Optional<Contract> result = contractRepository.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getContractNumber()).isEqualTo("CONT-2026-001");
        assertThat(result.get().getStatus()).isEqualTo(ContractStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should return empty Optional when ID does not exist")
    void findByIdShouldReturnEmpty() {
        UUID id = UUID.randomUUID();

        Optional<Contract> result = contractRepository.findById(id);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return all Contracts")
    void findAllShouldReturnAllContracts() {
        Client client = createClient("Hospital das Clínicas", "12345678000100");
        createContract("CONT-2026-001", ContractStatus.ACTIVE, client);
        createContract("CONT-2026-002", ContractStatus.SUSPENDED, client);
        createContract("CONT-2026-003", ContractStatus.CANCELLED, client);

        List<Contract> result = contractRepository.findAll();

        assertThat(result).hasSize(3);
    }
}