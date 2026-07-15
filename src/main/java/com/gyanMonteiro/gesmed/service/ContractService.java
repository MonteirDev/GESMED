package com.gyanMonteiro.gesmed.service;

import com.gyanMonteiro.gesmed.dto.request.ContractRequestDTO;
import com.gyanMonteiro.gesmed.dto.response.ContractResponseDTO;
import com.gyanMonteiro.gesmed.entity.Contract;
import com.gyanMonteiro.gesmed.enums.ContractStatus;
import com.gyanMonteiro.gesmed.exceptions.BusinessException;
import com.gyanMonteiro.gesmed.exceptions.ResourceNotFoundException;
import com.gyanMonteiro.gesmed.mapper.ContractMapper;
import com.gyanMonteiro.gesmed.repository.ContractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ContractService {

    @Autowired
    private ContractRepository repository;

    @Autowired
    private ContractMapper mapper;

    @Transactional
    public ContractResponseDTO create(ContractRequestDTO dto){
        Contract contract = mapper.toEntity(dto);
        repository.save(contract);
        return mapper.toResponse(contract);
    }

    public ContractResponseDTO updateEndDate(UUID id, ContractRequestDTO dto){
        Contract contract = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));
        contract.setEndDate(LocalDate.parse(dto.endDate()));
        repository.save(contract);
        return mapper.toResponse(contract);
    }

    public List<ContractResponseDTO> findAll(){
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public ContractResponseDTO findById(UUID id){
        Contract contract = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));
        return mapper.toResponse(contract);
    }

    public List<ContractResponseDTO> findByClientId(UUID id){
        Contract contracts = repository.findByClientId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contracts not found for client id: " + id));
        return List.of(mapper.toResponse(contracts));
    }

    public List<ContractResponseDTO> findByStatus(ContractStatus status){
        return repository.findByStatus(status)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public List<ContractResponseDTO> findByContractNumber(String contractNUmber){
        Contract contracts = repository.findByContractNumber(contractNUmber)
                .orElseThrow(() -> new ResourceNotFoundException("Contracts not found with contract number: " + contractNUmber));
        return List.of(mapper.toResponse(contracts));
    }

    public ContractResponseDTO setStatusCancelled(UUID id){
        Contract contract = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));
        if (contract.getStatus().equals(ContractStatus.CANCELLED) || contract.getStatus().equals(ContractStatus.EXPIRED)){
            throw new BusinessException("Contract is already cancelled");
        }
        contract.setStatus(ContractStatus.CANCELLED);
        repository.save(contract);
        return mapper.toResponse(contract);
    }

    public ContractResponseDTO setStatusSuspended(UUID id){
        Contract contract = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));
        if (contract.getStatus().equals(ContractStatus.SUSPENDED) || contract.getStatus().equals(ContractStatus.EXPIRED) || contract.getStatus().equals(ContractStatus.CANCELLED)){
            throw new BusinessException("Contract is already suspended");
        }
        contract.setStatus(ContractStatus.SUSPENDED);
        repository.save(contract);
        return mapper.toResponse(contract);
    }

    public ContractResponseDTO setStatusActive(UUID id){
        Contract contract = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));
        if (contract.getStatus().equals(ContractStatus.ACTIVE) || contract.getStatus().equals(ContractStatus.EXPIRED) || contract.getStatus().equals(ContractStatus.CANCELLED)){
            throw new BusinessException("Contract is already active");
        }
        contract.setStatus(ContractStatus.ACTIVE);
        repository.save(contract);
        return mapper.toResponse(contract);
    }
}
