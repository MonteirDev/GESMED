package com.gyanMonteiro.gesmed.controller;

import com.gyanMonteiro.gesmed.dto.request.ProductRequestDTO;
import com.gyanMonteiro.gesmed.dto.response.ProductResponseDTO;
import com.gyanMonteiro.gesmed.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private ProductService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN','COMPRAS')")
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody ProductRequestDTO dto){
        ProductResponseDTO response = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN', 'COMPRAS' ,'CONTRATOS', 'TELEVENDAS')")
    public ResponseEntity<ProductResponseDTO> getProductDetails(@PathVariable UUID id){
        ProductResponseDTO response = service.findById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN', 'COMPRAS')")
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable UUID id, @RequestBody ProductRequestDTO dto){
        ProductResponseDTO response = service.update(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN', 'COMPRAS')")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN', 'COMPRAS' ,'CONTRATOS', 'TELEVENDAS')")
    public ResponseEntity<List<ProductResponseDTO>> listALL(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String manufacturerName
    ){
        if (name != null) return ResponseEntity.ok(service.findByName(name));
        if (manufacturerName != null) return ResponseEntity.ok(service.findByManufacturer(manufacturerName));
        return ResponseEntity.ok(service.findAll());
    }
}
