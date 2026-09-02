package com.gyanMonteiro.gesmed.repository;

import com.gyanMonteiro.gesmed.entity.Client;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ClientRepositoryTest {

    @Autowired
    EntityManager entityManager;

    @Autowired
    ClientRepository clientRepository;

    private Client createClient(String name, String cnpj) {
        Client client = new Client();
        client.setName(name);
        client.setCnpj(cnpj);
        client.setAddresses(new ArrayList<>());
        entityManager.persist(client);
        return client;
    }

    @Test
    @DisplayName("Should get Client successfully from DB")
    void findByNameSuccess() {
        createClient("Hospital das Clínicas", "12345678000100");

        Optional<Client> result = clientRepository.findByNameIgnoreCase("Hospital das Clínicas");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Hospital das Clínicas");
    }

    @Test
    @DisplayName("Should not get Client from DB when client does not exist")
    void findByNameError() {
        Optional<Client> result = clientRepository.findByNameIgnoreCase("Inexistente");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty Optional when name does not match")
    void findByNameShouldReturnEmptyWhenNoMatch() {
        createClient("Hospital das Clínicas", "12345678000100");

        Optional<Client> result = clientRepository.findByNameIgnoreCase("Hospital Getúlio Vargas");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should get Client by CNPJ successfully from DB")
    void findByCnpjSuccess() {
        createClient("Hospital das Clínicas", "12345678000100");

        Optional<Client> result = clientRepository.findByCnpj("12345678000100");

        assertThat(result).isPresent();
        assertThat(result.get().getCnpj()).isEqualTo("12345678000100");
    }

    @Test
    @DisplayName("Should return empty Optional when CNPJ does not exist")
    void findByCnpjError() {
        Optional<Client> result = clientRepository.findByCnpj("00000000000000");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return Client when ID exists")
    void findByIdShouldReturnClient() {
        Client created = createClient("Hospital das Clínicas", "12345678000100");
        UUID id = created.getId();

        Optional<Client> result = clientRepository.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Hospital das Clínicas");
        assertThat(result.get().getCnpj()).isEqualTo("12345678000100");
    }

    @Test
    @DisplayName("Should return empty Optional when ID does not exist")
    void findByIdShouldReturnEmpty() {
        UUID id = UUID.randomUUID();

        Optional<Client> result = clientRepository.findById(id);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return all Clients")
    void findAllShouldReturnAllClients() {
        createClient("Hospital das Clínicas", "12345678000100");
        createClient("Hospital Getúlio Vargas", "98765432000100");
        createClient("UPA Recife", "11122233000100");

        List<Client> result = clientRepository.findAll();

        assertThat(result).hasSize(3);
    }
}