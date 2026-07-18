package com.gyanMonteiro.gesmed.controller;

import com.gyanMonteiro.gesmed.dto.request.ManufacturerRequestDTO;
import com.gyanMonteiro.gesmed.dto.request.update.ManufacturerUpdateRequestDTO;
import com.gyanMonteiro.gesmed.dto.response.ManufacturerResponseDTO;
import com.gyanMonteiro.gesmed.service.ManufacturerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/manufacturer")
public class ManufacturerController {
    @Autowired
    private ManufacturerService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMPRAS')")
    public ResponseEntity<ManufacturerResponseDTO> createManufacturer(@Valid @RequestBody ManufacturerRequestDTO dto){
        ManufacturerResponseDTO response = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPRAS')")
    public ResponseEntity<ManufacturerResponseDTO> getManufacturerDetails(@PathVariable UUID id){
        ManufacturerResponseDTO response = service.findById(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPRAS')")
    public ResponseEntity<ManufacturerResponseDTO> updateManufacturer(@PathVariable UUID id, @RequestBody ManufacturerUpdateRequestDTO dto){
        ManufacturerResponseDTO response = service.update(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPRAS')")
    public ResponseEntity<Void> deleteManufacturer(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cnpj/{cnpj}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPRAS')")
    public ResponseEntity<ManufacturerResponseDTO> listByCnpj(@PathVariable String cnpj){
        return ResponseEntity.ok(service.findByCnpj(cnpj));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPRAS')")
    public ResponseEntity<List<ManufacturerResponseDTO>> listALL(@RequestParam(required = false) String name){
        if (name != null) return ResponseEntity.ok(service.findByName(name));
        return ResponseEntity.ok(service.findAll());
    }
}
