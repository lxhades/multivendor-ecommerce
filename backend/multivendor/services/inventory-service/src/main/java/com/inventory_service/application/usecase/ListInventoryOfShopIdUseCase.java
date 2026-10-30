package com.inventory_service.application.usecase;

import com.inventory_service.application.dto.InventoryResponse;
import com.inventory_service.domain.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListInventoryOfShopIdUseCase {

    private final InventoryRepository inventoryRepository;

    public ListInventoryOfShopIdUseCase(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> execute(UUID shopId) {

        return inventoryRepository.findByShopId(shopId)
                .stream()
                .map(InventoryResponse::from)
                .toList();
    }
}