package com.gyanMonteiro.gesmed.mapper;

import com.gyanMonteiro.gesmed.dto.request.ManufacturerRequestDTO;
import com.gyanMonteiro.gesmed.dto.response.ManufacturerResponseDTO;
import com.gyanMonteiro.gesmed.entity.Manufacturer;
import org.springframework.stereotype.Component;

@Component
public class ManufacturerMapper {
    public Manufacturer toEntity(ManufacturerRequestDTO dto){
        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setName(dto.name());
        manufacturer.setCnpj(dto.cnpj());
        manufacturer.setActive(true);
        return manufacturer;
    }
    public ManufacturerResponseDTO toResponse (Manufacturer manufacturer){
        return new ManufacturerResponseDTO(
                manufacturer.getId(),
                manufacturer.getName(),
                formatCnpj(manufacturer.getCnpj()),
                manufacturer.getCreatedAt(),
                manufacturer.isActive()
        );
    }

    private String formatCnpj(String cnpj) {
        if (cnpj == null || cnpj.length() != 14) return cnpj;
        return cnpj.replaceAll("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
    }
}
