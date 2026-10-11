package com.gyanMonteiro.gesmed.service;

import com.gyanMonteiro.gesmed.dto.request.StockRequestDTO;
import com.gyanMonteiro.gesmed.dto.response.StockOperationResultDTO;
import com.gyanMonteiro.gesmed.dto.response.StockResponseDTO;
import com.gyanMonteiro.gesmed.entity.Manufacturer;
import com.gyanMonteiro.gesmed.entity.Product;
import com.gyanMonteiro.gesmed.entity.Stock;
import com.gyanMonteiro.gesmed.exceptions.BusinessException;
import com.gyanMonteiro.gesmed.exceptions.ResourceNotFoundException;
import com.gyanMonteiro.gesmed.mapper.StockMapper;
import com.gyanMonteiro.gesmed.repository.ProductRepository;
import com.gyanMonteiro.gesmed.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class StockService {
    @Autowired
    private StockRepository repository;

    @Autowired
    private StockMapper mapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SupplierPurchaseHistoryService supplierPurchaseHistoryService;

    @Transactional
    public StockResponseDTO createEntry(StockRequestDTO dto) {
        Product product = productRepository.findById(dto.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        Manufacturer manufacturer = product.getManufacturer();

        if (repository.existsByBatchNumber(dto.batchNumber())) {
            throw new BusinessException("Batch number already exists");
        }

        Stock stock = mapper.toEntity(dto, product);
        stock = repository.save(stock);

        supplierPurchaseHistoryService.create(dto, product, manufacturer, stock);

        return mapper.toResponse(stock);
    }

    public StockResponseDTO findById(UUID id){
        Stock entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found"));
        return mapper.toResponse(entity);
    }

    public List<StockResponseDTO> findByProduct(UUID id){
        return repository.findByProductId(id)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public Integer getAvailableQuantity(UUID id) {
        return repository.findAvailableQuantityByProductId(id)
                .orElse(0);
    }

    public List<StockResponseDTO> findAvailableByProductFEFO(UUID id){
        return repository.findAvailableStockByProductOrderByExpiry(id)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public List<StockResponseDTO> findExpiringSoon(LocalDate limitDate){
        return repository.findByExpiryDateBefore(limitDate)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public StockOperationResultDTO reserveQuantity(UUID id, Integer quantity){
        List<Stock> stocks = repository.findAvailableStockByProductOrderByExpiry(id);
        
        Integer remaining = quantity;

        for (Stock batch : stocks){
            if (remaining <= 0) break;

            Integer batchAvailable = batch.getQuantity() - batch.getReserveQuantity();
            Integer toReserve = Math.min(batchAvailable, remaining);

            batch.setReserveQuantity(batch.getReserveQuantity() + toReserve);
            repository.save(batch);
            remaining -= toReserve;
        }
        if (remaining > 0) {
            throw new BusinessException("Not enough stock available to reserve the requested quantity.");
        }

        Integer available = repository.findAvailableQuantityByProductId(id).orElse(0);
        return new StockOperationResultDTO(
                "Reservation completed successfully",
                id,
                quantity,
                available
        );
    }

    @Transactional
    public StockOperationResultDTO releaseReservedQuantity(UUID id, Integer quantity){
        List<Stock> reservedBatches = repository.findByProductId(id)
                .stream()
                .filter(batch -> batch.getReserveQuantity() > 0)
                .toList();

        Integer remaining = quantity;

        for (Stock batch : reservedBatches){
            if (remaining <= 0) break;

            Integer toRelease = Math.min(batch.getReserveQuantity(), remaining);

            batch.setReserveQuantity(batch.getReserveQuantity() - toRelease);
            repository.save(batch);

            remaining -= toRelease;
        }
        if (remaining > 0) {
            throw new BusinessException("Not enough reserved stock to release the requested quantity.");
        }
        Integer available = repository.findAvailableQuantityByProductId(id).orElse(0);
        return new StockOperationResultDTO(
                "Reservation released successfully",
                id,
                quantity,
                available
        );
    }

    @Transactional
    public StockOperationResultDTO dispatchStock(UUID id, Integer quantity){
        List<Stock> availableBatches = repository.findByProductIdAndQuantityGreaterThanOrderByExpiryDateAsc(id, 0);

        Integer remaining = quantity;

        for (Stock batch : availableBatches){
            if (remaining <= 0) break;
            if (batch.getReserveQuantity() <= 0) continue;

            Integer toDispatch = Math.min(batch.getReserveQuantity(), remaining);

            batch.setQuantity(batch.getQuantity() - toDispatch);
            batch.setReserveQuantity(batch.getReserveQuantity() - toDispatch);
            repository.save(batch);

            remaining -= toDispatch;
        }
        if (remaining > 0) {
            throw new BusinessException("Not enough stock available to dispatch the requested quantity.");
        }

        Integer available = repository.findAvailableQuantityByProductId(id).orElse(0);
        return new StockOperationResultDTO(
                "Dispatch completed successfully",
                id,
                quantity,
                available
        );
    }
}
