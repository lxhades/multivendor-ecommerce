package com.inventory_service.application.inventory.usecase;

import com.inventory_service.application.inventory.dto.InventoryResponse;
import com.inventory_service.domain.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetInventoryUseCase {

    private final InventoryRepository inventoryRepository;

    public GetInventoryUseCase(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional(readOnly = true)
    public InventoryResponse getById(UUID inventoryId) {

        return inventoryRepository.findById(inventoryId)
                .map(InventoryResponse::from)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy kho có id:  " + inventoryId
                ));
    }

    @Transactional(readOnly = true)
    public InventoryResponse getByVariantId(UUID variantId) {

        return inventoryRepository.findByVariantId(variantId)
                .map(InventoryResponse::from)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tim thấy kho của varient: " + variantId
                ));
    }
}