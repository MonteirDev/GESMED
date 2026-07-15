package com.gyanMonteiro.gesmed.mapper;

import com.gyanMonteiro.gesmed.dto.request.ContractItemsRequestDTO;
import com.gyanMonteiro.gesmed.dto.request.ContractRequestDTO;
import com.gyanMonteiro.gesmed.dto.response.ClientSummaryResponseDTO;
import com.gyanMonteiro.gesmed.dto.response.ContractItemResponseDTO;
import com.gyanMonteiro.gesmed.dto.response.ContractResponseDTO;
import com.gyanMonteiro.gesmed.dto.response.ProductSummaryResponseDTO;
import com.gyanMonteiro.gesmed.entity.Contract;
import com.gyanMonteiro.gesmed.entity.ContractItems;
import com.gyanMonteiro.gesmed.entity.Product;
import com.gyanMonteiro.gesmed.enums.ContractStatus;
import com.gyanMonteiro.gesmed.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class ContractMapper {

    @Autowired
    ProductRepository productRepository;

    public Contract toEntity(ContractRequestDTO dto){
        Contract contract = new Contract();
        contract.setContractNumber(dto.contractNumber());
        contract.setStartDate(LocalDate.parse(dto.startDate()));
        contract.setEndDate(LocalDate.parse(dto.endDate()));
        contract.setStatus(ContractStatus.ACTIVE);
        List<ContractItems> contractItems = dto.contractItems()
                .stream()
                .map(ItemsDto -> toItemEntity(ItemsDto, contract))
                .toList();
        contract.setContractItems(contractItems);
        return contract;
    }

    public ContractItems toItemEntity(ContractItemsRequestDTO dto, Contract contract){
        ContractItems item = new ContractItems();
        item.setUnitPrice(dto.unitPrice());
        item.setTotalQuantity(dto.totalQuantity());
        item.setBalanceQuantity(dto.balanceQuantity());
        Product product = productRepository.findById(dto.productId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        item.setProduct(product);
        item.setContract(contract);
        return item;
    }

    public ContractItemResponseDTO toItemResposnse(ContractItems contractItems){
        return new ContractItemResponseDTO(
                contractItems.getId(),
                new ProductSummaryResponseDTO(
                        contractItems.getProduct().getName(),
                        contractItems.getProduct().getManufacturer().getName(),
                        contractItems.getProduct().getSku(),
                        contractItems.getProduct().getUnitofMeasure(),
                        contractItems.getProduct().getDosage()
                ),
                contractItems.getUnitPrice(),
                contractItems.getTotalQuantity(),
                contractItems.getBalanceQuantity(),
                contractItems.getCreatedAt()
        );
    }

    public ContractResponseDTO toResponse(Contract contract){
        List<ContractItemResponseDTO> items = contract.getContractItems()
                .stream()
                .map(this::toItemResposnse)
                .toList();
        return new ContractResponseDTO(
                contract.getId(),
                contract.getContractNumber(),
                contract.getStartDate(),
                contract.getEndDate(),
                contract.getStatus(),
                contract.getCreatedAt(),
                new ClientSummaryResponseDTO(
                        contract.getClient().getName(),
                        contract.getClient().getCnpj(),
                        contract.getClient().isActive()
                ),
                items
        );
    }
}
