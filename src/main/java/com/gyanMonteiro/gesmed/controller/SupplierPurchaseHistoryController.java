package com.gyanMonteiro.gesmed.controller;


import com.gyanMonteiro.gesmed.dto.response.SupplierPurchaseHistoryResponseDTO;
import com.gyanMonteiro.gesmed.service.SupplierPurchaseHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/supplier-purchase-history")
public class SupplierPurchaseHistoryController {

    @Autowired
    private SupplierPurchaseHistoryService supplierService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<SupplierPurchaseHistoryResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(supplierService.findById(id));
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<SupplierPurchaseHistoryResponseDTO>> findByProduct(@PathVariable UUID productId){
        return ResponseEntity.ok(supplierService.findByProduct(productId));
    }

    @GetMapping("/manufacturer/{manufacturerId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<SupplierPurchaseHistoryResponseDTO>> findByManufacturer(@PathVariable UUID manufacturerId){
        return ResponseEntity.ok(supplierService.findByManufacturer(manufacturerId));
    }

    @GetMapping("/manufacturer/{manufacturerId}/paginated")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Page<SupplierPurchaseHistoryResponseDTO>> findByManufacturerPaginated(@PathVariable UUID manufacturerId, Pageable pageable){
        return ResponseEntity.ok(supplierService.findByManufacturerPaginated(manufacturerId, pageable));
    }

    @GetMapping("/period")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity <List<SupplierPurchaseHistoryResponseDTO>> findByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end){
        return ResponseEntity.ok(supplierService.findByPeriod(start, end));
    }

    @GetMapping("/product/{productId}/average-cost")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<BigDecimal> getAverageCost(
            @PathVariable UUID productId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end){
        return  ResponseEntity.ok(supplierService.getAveragePurchasePriceByProduct(productId, start, end));
    }
}
