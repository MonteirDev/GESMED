package com.gyanMonteiro.gesmed.service;

import com.gyanMonteiro.gesmed.dto.request.StockRequestDTO;
import com.gyanMonteiro.gesmed.dto.response.SupplierPurchaseHistoryResponseDTO;
import com.gyanMonteiro.gesmed.entity.Manufacturer;
import com.gyanMonteiro.gesmed.entity.Product;
import com.gyanMonteiro.gesmed.entity.Stock;
import com.gyanMonteiro.gesmed.entity.SupplierPurchaseHistory;
import com.gyanMonteiro.gesmed.exceptions.ResourceNotFoundException;
import com.gyanMonteiro.gesmed.mapper.SupplierPurchaseHistoryMapper;
import com.gyanMonteiro.gesmed.repository.SupplierPurchaseHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class SupplierPurchaseHistoryService {

    @Autowired
    private SupplierPurchaseHistoryRepository repository;

    @Autowired
    private SupplierPurchaseHistoryMapper mapper;

    public SupplierPurchaseHistory create(StockRequestDTO dto, Product product, Manufacturer manufacturer, Stock stock){
        SupplierPurchaseHistory entity = mapper.toEntity(dto, product, manufacturer, stock);
        return repository.save(entity);
    }

    public SupplierPurchaseHistoryResponseDTO findById(UUID id){
        SupplierPurchaseHistory entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier Purchase History not found"));
        return mapper.toResponse(entity);
    }

    public List<SupplierPurchaseHistoryResponseDTO> findByProduct(UUID id){
        return repository.findByProductId(id)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public List<SupplierPurchaseHistoryResponseDTO> findByManufacturer(UUID id){
        return repository.findByManufacturerId(id)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public Page<SupplierPurchaseHistoryResponseDTO> findByManufacturerPaginated(UUID manufacturerId, Pageable pageable){
        return repository.findByManufacturerIdOrderByPurchaseDateDesc(manufacturerId, pageable)
                .map(mapper::toResponse);
    }

    public List<SupplierPurchaseHistoryResponseDTO> findByPeriod(LocalDate start, LocalDate end){
        return repository.findByPurchaseDateBetween(start, end)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public BigDecimal getAveragePurchasePriceByProduct(UUID productId,  LocalDate start, LocalDate end){
        return repository.findAverageUnitCostByProduct(productId, start, end)
                .orElse(BigDecimal.ZERO);
    }
}
