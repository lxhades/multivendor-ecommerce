package com.order_service.application.checkout.command;

import java.util.Objects;
import java.util.UUID;

public record OrderItemCommand(UUID productId, UUID variantId, String productName, long unitPrice, int quantity) {
    public OrderItemCommand {
        Objects.requireNonNull(productId);
        Objects.requireNonNull(variantId);
        if (productName == null || productName.isBlank() || unitPrice < 0 || quantity <= 0)
            throw new IllegalArgumentException("Invalid item");
    }
}
