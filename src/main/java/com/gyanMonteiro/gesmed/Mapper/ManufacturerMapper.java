package com.gyanMonteiro.gesmed.Mapper;

import com.gyanMonteiro.gesmed.RequestDTO.ManufacturerRequestDTO;
import com.gyanMonteiro.gesmed.ResponseDTO.ManufacturerCreateResponseDTO;
import com.gyanMonteiro.gesmed.ResponseDTO.ManufacturerResponseDTO;
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
    public ManufacturerResponseDTO toResponde (Manufacturer manufacturer){
        return new ManufacturerResponseDTO(
                manufacturer.getId(),
                manufacturer.getName(),
                manufacturer.getCnpj(),
                manufacturer.getCreatedAt(),
                manufacturer.isActive()
        );
    }

    public ManufacturerCreateResponseDTO toCreateResponse(Manufacturer manufacturer){
        return new ManufacturerCreateResponseDTO(
                manufacturer.getId()
        );
    }
}
