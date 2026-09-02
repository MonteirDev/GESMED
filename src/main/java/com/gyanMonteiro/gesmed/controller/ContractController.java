package com.gyanMonteiro.gesmed.controller;


import com.gyanMonteiro.gesmed.dto.request.ContractRequestDTO;
import com.gyanMonteiro.gesmed.dto.response.ContractResponseDTO;
import com.gyanMonteiro.gesmed.enums.ContractStatus;
import com.gyanMonteiro.gesmed.service.ContractService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/contract")
public class ContractController {

    @Autowired
    private ContractService contractService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONTRATOS')")
    public ResponseEntity<ContractResponseDTO> createContract(@Valid @RequestBody ContractRequestDTO dto){
        ContractResponseDTO response = contractService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONTRATOS', 'FINANCEIRO', 'TELEVENDAS', 'FATURAMENTO')")
    public ResponseEntity<ContractResponseDTO> getContractDetails(@PathVariable UUID id){
        ContractResponseDTO response = contractService.findById(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/end-date")
    @PreAuthorize("hasAnyRole('ADMIN','CONTRATOS', 'FINANCEIRO')")
    public ResponseEntity<ContractResponseDTO> patchEndDate(@PathVariable UUID id, @Valid @RequestBody ContractRequestDTO dto){
        ContractResponseDTO response = contractService.updateEndDate(id, dto);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','CONTRATOS', 'FINANCEIRO')")
    public ResponseEntity<ContractResponseDTO> setStatusCancelled(@PathVariable UUID id){
        ContractResponseDTO response = contractService.setStatusCancelled(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/suspend")
    @PreAuthorize("hasAnyRole('ADMIN','CONTRATOS', 'FINANCEIRO')")
    public ResponseEntity<ContractResponseDTO> setStatusSuspended(@PathVariable UUID id){
        ContractResponseDTO response = contractService.setStatusSuspended(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN','CONTRATOS', 'FINANCEIRO')")
    public ResponseEntity<ContractResponseDTO> setStatusActivate(@PathVariable UUID id){
        ContractResponseDTO response = contractService.setStatusActive(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONTRATOS', 'FINANCEIRO', 'TELEVENDAS', 'FATURAMENTO')")
    public ResponseEntity<List<ContractResponseDTO>> listAll(
            @RequestParam(required = false) UUID cliendId,
            @RequestParam(required = false)ContractStatus status,
            @RequestParam(required = false) String contractNumber){
        if (cliendId != null) return ResponseEntity.ok(contractService.findByClientId(cliendId));
        if (status != null) return ResponseEntity.ok(contractService.findByStatus(status));
        if (contractNumber != null) return ResponseEntity.ok(contractService.findByContractNumber(contractNumber));
        return ResponseEntity.ok(contractService.findAll());
    }
}
