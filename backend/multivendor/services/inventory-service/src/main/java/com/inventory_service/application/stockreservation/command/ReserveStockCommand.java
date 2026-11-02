package com.inventory_service.application.stockreservation.command;

import java.util.UUID;

public record ReserveStockCommand(
        UUID orderId,
        UUID variantId,
        long quantity
) {
    public ReserveStockCommand {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId is required");
        }
        if (variantId == null) {
            throw new IllegalArgumentException("variantId is required");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }
}