package com.inventory_service.application.stockreservation.command;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record ReserveCheckoutStockCommand(
        UUID checkoutId,
        List<Item> items
) {
    public ReserveCheckoutStockCommand {
        Objects.requireNonNull(checkoutId);
        Objects.requireNonNull(items);

        items = List.copyOf(items);

        if (items.isEmpty()) {
            throw new IllegalArgumentException("Empty items");
        }
    }

    public record Item(
            UUID orderId,
            UUID variantId,
            long quantity
    ) {
    }
}
