package com.gyanMonteiro.gesmed.controller;

import com.gyanMonteiro.gesmed.RequestDTO.ManufacturerRequestDTO;
import com.gyanMonteiro.gesmed.ResponseDTO.ManufacturerCreateResponseDTO;
import com.gyanMonteiro.gesmed.ResponseDTO.ManufacturerResponseDTO;
import com.gyanMonteiro.gesmed.Service.ManufacturerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/manufacturer")
public class ManufacturerController {
    @Autowired
    private ManufacturerService service;

    @PostMapping
    public ResponseEntity<ManufacturerCreateResponseDTO> createManufacturer(@RequestBody ManufacturerRequestDTO dto){
        ManufacturerCreateResponseDTO response = service.create(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ManufacturerResponseDTO> getManufacturerDetails(@PathVariable UUID id){
        ManufacturerResponseDTO response = service.findById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ManufacturerResponseDTO> updateManufacturer(@PathVariable UUID id, @RequestBody ManufacturerRequestDTO dto){
        ManufacturerResponseDTO response = service.update(id, dto);
        return ResponseEntity.ok(response);
    }
}
