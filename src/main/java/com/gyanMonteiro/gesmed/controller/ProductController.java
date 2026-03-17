package com.gyanMonteiro.gesmed.controller;

import com.gyanMonteiro.gesmed.RequestDTO.ProductRequestDTO;
import com.gyanMonteiro.gesmed.ResponseDTO.ProductCreateResponseDTO;
import com.gyanMonteiro.gesmed.ResponseDTO.ProductResponseDTO;
import com.gyanMonteiro.gesmed.Service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private ProductService service;

    @PostMapping
    public ResponseEntity<ProductCreateResponseDTO> createProduct(@RequestBody ProductRequestDTO dto){
        ProductCreateResponseDTO response = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductDetails(@PathVariable UUID id){
        ProductResponseDTO response = service.findById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable UUID id, @RequestBody ProductRequestDTO dto){
        ProductResponseDTO response = service.update(id, dto);
        return ResponseEntity.ok(response);
    }
}
