package com.gyanMonteiro.gesmed.repository;

import com.gyanMonteiro.gesmed.Mapper.ManufacturerMapper;
import com.gyanMonteiro.gesmed.RequestDTO.ManufacturerRequestDTO;
import com.gyanMonteiro.gesmed.entity.Manufacturer;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(ManufacturerMapper.class)
@ActiveProfiles("test")
class ManufacturerRepositoryTest {

    @Autowired
    EntityManager entityManager;

    @Autowired
    ManufacturerRepository manufacturerRepository;

    @Autowired
    ManufacturerMapper manufacturerMapper;

    @Test
    @DisplayName("Should get Manufacturer successfully from DB")
    void findByNameSuccess() {
        String cnpj = "02.814.497/0001-07";
        String name = "CIMED";
        ManufacturerRequestDTO dto = new ManufacturerRequestDTO(name, cnpj);
        this.createManufacturer(dto);
        Optional<Manufacturer> result = this.manufacturerRepository.findByName(name);
        assertThat(result.isPresent()).isTrue();
    }

    @Test
    @DisplayName("Should not get Manufacturer from DB when manufacturer not exists")
    void findByNameError() {
        String name = "CIMED";
        Optional<Manufacturer> result = this.manufacturerRepository.findByName(name);
        assertThat(result.isEmpty()).isTrue();
    }

    private Manufacturer createManufacturer(ManufacturerRequestDTO dto){
        Manufacturer newManufacturer = manufacturerMapper.toEntity(dto);
        this.entityManager.persist(newManufacturer);
        return newManufacturer;
    }
}