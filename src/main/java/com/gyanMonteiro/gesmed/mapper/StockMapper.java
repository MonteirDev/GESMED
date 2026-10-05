package com.gyanMonteiro.gesmed.mapper;


import com.gyanMonteiro.gesmed.dto.request.StockRequestDTO;
import com.gyanMonteiro.gesmed.dto.response.ProductSummaryResponseDTO;
import com.gyanMonteiro.gesmed.dto.response.StockResponseDTO;
import com.gyanMonteiro.gesmed.entity.Product;
import com.gyanMonteiro.gesmed.entity.Stock;
import org.springframework.stereotype.Component;

@Component
public class StockMapper {

    public Stock toEntity(StockRequestDTO dto, Product product){
        Stock entity = new Stock();
        entity.setBatchNumber(dto.batchNumber());
        entity.setExpiryDate(dto.expiryDate());
        entity.setQuantity(dto.quantity());
        entity.setReserveQuantity(0);
        entity.setProduct(product);
        return entity;
    }

    public StockResponseDTO toResponse(Stock entity){
        return new StockResponseDTO(
                entity.getId(),
                new ProductSummaryResponseDTO(
                        entity.getProduct().getName(),
                        entity.getProduct().getManufacturer().getName(),
                        entity.getProduct().getSku(),
                        entity.getProduct().getUnitofMeasure(),
                        entity.getProduct().getDosage()
                ),
                entity.getBatchNumber(),
                entity.getExpiryDate(),
                entity.getQuantity(),
                entity.getReserveQuantity(),
                entity.getCreatedAt()
        );
    }
}
