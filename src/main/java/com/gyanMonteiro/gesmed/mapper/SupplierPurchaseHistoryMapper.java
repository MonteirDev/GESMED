package com.gyanMonteiro.gesmed.mapper;

import com.gyanMonteiro.gesmed.dto.request.StockRequestDTO;
import com.gyanMonteiro.gesmed.dto.response.ManufacturerSummaryResponseDTO;
import com.gyanMonteiro.gesmed.dto.response.ProductBasicSummaryResponseDTO;
import com.gyanMonteiro.gesmed.dto.response.SupplierPurchaseHistoryResponseDTO;
import com.gyanMonteiro.gesmed.entity.Manufacturer;
import com.gyanMonteiro.gesmed.entity.Product;
import com.gyanMonteiro.gesmed.entity.Stock;
import com.gyanMonteiro.gesmed.entity.SupplierPurchaseHistory;
import org.springframework.stereotype.Component;

@Component
public class SupplierPurchaseHistoryMapper {

    public SupplierPurchaseHistory toEntity(StockRequestDTO dto, Product product, Manufacturer manufacturer, Stock stock){
        SupplierPurchaseHistory entity = new SupplierPurchaseHistory();
        entity.setProduct(product);
        entity.setManufacturer(manufacturer);
        entity.setStock(stock);
        entity.setBatchNumber(dto.batchNumber());
        entity.setQuantity(dto.quantity());
        entity.setUnitPrice(dto.unitCost());
        entity.setPurchaseDate(dto.purchaseDate());
        return entity;
    }

    public SupplierPurchaseHistoryResponseDTO toResponse(SupplierPurchaseHistory entity){
        return new SupplierPurchaseHistoryResponseDTO(
                entity.getId(),
                new ProductBasicSummaryResponseDTO(
                        entity.getProduct().getName(),
                        entity.getProduct().getSku(),
                        entity.getProduct().getUnitofMeasure(),
                        entity.getProduct().getDosage()
                ),
                new ManufacturerSummaryResponseDTO(
                        entity.getManufacturer().getName(),
                        entity.getManufacturer().isActive()
                ),
                entity.getBatchNumber(),
                entity.getQuantity(),
                entity.getUnitPrice(),
                entity.getPurchaseDate(),
                entity.getCreatedAt()
        );
    }
}
