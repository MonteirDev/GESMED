package com.gyanMonteiro.gesmed.controller;


import com.gyanMonteiro.gesmed.dto.request.StockRequestDTO;
import com.gyanMonteiro.gesmed.dto.response.StockOperationResultDTO;
import com.gyanMonteiro.gesmed.dto.response.StockResponseDTO;
import com.gyanMonteiro.gesmed.service.StockService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/stock")
public class StockController {
    @Autowired
    private StockService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<StockResponseDTO> createStock(@Valid @RequestBody StockRequestDTO dto){
        StockResponseDTO response = service.createEntry(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<StockResponseDTO> getStockDetails(@PathVariable UUID id){
        StockResponseDTO response = service.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<StockResponseDTO>> getStockByProduct(@PathVariable UUID productId){
        List<StockResponseDTO> response = service.findByProduct(productId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/product/{productId}/available")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Integer> getAvailableQuantityProduct(@PathVariable UUID productId){
        Integer response = service.getAvailableQuantity(productId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/product/{productId}/fefo")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<StockResponseDTO>> getStockByProductFefo(@PathVariable UUID productId){
        List<StockResponseDTO> response = service.findAvailableByProductFEFO(productId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/expiring-soon")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<StockResponseDTO>> getStockExpiringSoon(
            @RequestParam(name = "limitDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate limitDate){
        List<StockResponseDTO> response = service.findExpiringSoon(limitDate);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{productId}/dispatch")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<StockOperationResultDTO> getStockForDispatch(@PathVariable UUID productId, @RequestParam Integer quantity){
        StockOperationResultDTO response = service.dispatchStock(productId, quantity);
        return ResponseEntity.ok(response);
    }

}
