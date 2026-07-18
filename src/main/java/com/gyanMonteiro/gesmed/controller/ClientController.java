package com.gyanMonteiro.gesmed.controller;

import com.gyanMonteiro.gesmed.dto.request.ClientAddressRequestDTO;
import com.gyanMonteiro.gesmed.dto.request.ClientRequestDTO;
import com.gyanMonteiro.gesmed.dto.response.ClientResponseDTO;
import com.gyanMonteiro.gesmed.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/client")
public class ClientController {
    @Autowired
    private ClientService clientService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCEIRO')")
    public ResponseEntity<ClientResponseDTO> createClient(@Valid @RequestBody ClientRequestDTO dto){
        ClientResponseDTO response = clientService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONTRATOS')")
    public ResponseEntity<ClientResponseDTO> getClientDetails(@PathVariable UUID id){
        ClientResponseDTO response = clientService.findById(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCEIRO')")
    public ResponseEntity<ClientResponseDTO> updateClient(@PathVariable UUID id,@Valid @RequestBody ClientRequestDTO dto){
        ClientResponseDTO response = clientService.update(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCEIRO')")
    public ResponseEntity<Void> deleteClient(@PathVariable UUID id){
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/address")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCEIRO')")
    public ResponseEntity<ClientResponseDTO> createAddress(@PathVariable UUID id, @Valid @RequestBody ClientAddressRequestDTO dto){
        ClientResponseDTO response = clientService.addAddress(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{clientId}/address/{addressId}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCEIRO')")
    public ResponseEntity<ClientResponseDTO> updateAddress(@PathVariable UUID id, @PathVariable UUID clientId, @Valid @RequestBody ClientAddressRequestDTO dto){
        ClientResponseDTO response = clientService.updateAddress(id, clientId, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{clientId}/address/{addressId}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCEIRO')")
    public ResponseEntity<Void> deleteAddress(@PathVariable UUID clientId, @PathVariable UUID addressId){
        clientService.deleteAddress(clientId, addressId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONTRATOS')")
    public ResponseEntity<List<ClientResponseDTO>> listAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String cnpj){
        if (name != null) return ResponseEntity.ok(clientService.findByName(name));
        if (cnpj != null) return ResponseEntity.ok(clientService.findByCnpj(cnpj));
        return ResponseEntity.ok(clientService.findAll());
    }
}
