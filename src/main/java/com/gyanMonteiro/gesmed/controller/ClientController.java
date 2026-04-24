package com.gyanMonteiro.gesmed.controller;

import com.gyanMonteiro.gesmed.dto.request.ClientRequestDTO;
import com.gyanMonteiro.gesmed.dto.response.ClientResponseDTO;
import com.gyanMonteiro.gesmed.service.ClientService;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
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
    @PreAuthorize("hasRole('ADMIN','FINANCEIRO')")
    public ResponseEntity<ClientResponseDTO> createClient(@Valid @RequestBody ClientRequestDTO dto){
        ClientResponseDTO response = clientService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN','CONTRATOS')")
    public ResponseEntity<ClientResponseDTO> getClientDetails(@PathVariable UUID id){
        ClientResponseDTO response = clientService.findById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN','FINANCEIRO')")
    public ResponseEntity<ClientResponseDTO> updateClient(@PathVariable UUID id, @RequestBody ClientRequestDTO dto){
        ClientResponseDTO response = clientService.update(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN','FINANCEIRO')")
    public ResponseEntity<Void> deleteClient(@PathVariable UUID id){
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN','CONTRATOS')")
    public ResponseEntity<List<ClientResponseDTO>> listAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String cnpj){
        if (name != null) return ResponseEntity.ok(clientService.findByName(name));
        if (cnpj != null) return ResponseEntity.ok(clientService.findByCnpj(cnpj));
        return ResponseEntity.ok(clientService.findAll());
    }
}
