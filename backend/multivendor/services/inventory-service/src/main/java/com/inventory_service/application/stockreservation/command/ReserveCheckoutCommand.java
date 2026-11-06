package com.inventory_service.application.stockreservation.command;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record ReserveCheckoutCommand(
        UUID eventId,
        UUID checkoutId,
        UUID buyerId,
        List<Item> items
) {
    public ReserveCheckoutCommand {
        Objects.requireNonNull(eventId, "eventId is required");
        Objects.requireNonNull(checkoutId, "checkoutId is required");
        Objects.requireNonNull(buyerId, "buyerId is required");
        Objects.requireNonNull(items, "items is required");

        items = List.copyOf(items);

        if (items.isEmpty()) {
            throw new IllegalArgumentException("items cannot be empty");
        }

        Set<String> uniqueItems = new HashSet<>();

        for (Item item : items) {
            Objects.requireNonNull(item, "item cannot be null");

            String key = item.orderId() + ":" + item.variantId();

            if (!uniqueItems.add(key)) {
                throw new IllegalArgumentException(
                        "Duplicate orderId/variantId: " + key
                );
            }
        }
    }

    public record Item(
            UUID orderId,
            UUID variantId,
            long quantity
    ) {
        public Item {
            Objects.requireNonNull(orderId);
            Objects.requireNonNull(variantId);

            if (quantity <= 0) {
                throw new IllegalArgumentException(
                        "quantity must be positive"
                );
            }
        }
    }
}