package com.inventory_service.application.inventory.dto;



import com.inventory_service.domain.model.aggregate.Inventory;

import java.time.Instant;
import java.util.UUID;

public record InventoryResponse(
        UUID id,
        UUID productId,
        UUID variantId,
        UUID shopId,
        long onHandQuantity,
        long reservedQuantity,
        long availableQuantity,
        Instant createdAt,
        Instant updatedAt
) {
    public static InventoryResponse from(Inventory inventory) {
        return new InventoryResponse(
                inventory.getId(),
                inventory.getProductId(),
                inventory.getVariantId(),
                inventory.getShopId(),
                inventory.getOnHandQuantity(),
                inventory.getReservedQuantity(),
                inventory.availableQuantity(),
                inventory.getCreatedAt(),
                inventory.getUpdatedAt()
        );
    }
}