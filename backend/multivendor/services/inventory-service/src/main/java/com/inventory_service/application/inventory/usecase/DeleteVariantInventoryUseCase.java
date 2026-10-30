package com.inventory_service.application.inventory.usecase;

import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import com.inventory_service.domain.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteVariantInventoryUseCase {
    private final InventoryRepository inventoryRepository;
    public DeleteVariantInventoryUseCase(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }
    @Transactional
    public void delete(UUID variantId){
        boolean exists = inventoryRepository.existsByVariantId(variantId);
        if(!exists){
            throw  new DomainException(ErrorCode.VARIENT_INVENTORY_NOT_FOUND);
        }
        inventoryRepository.deleteByVariantId(variantId);
    }
}
