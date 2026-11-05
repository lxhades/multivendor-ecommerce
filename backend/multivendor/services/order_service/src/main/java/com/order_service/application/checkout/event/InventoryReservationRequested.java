
package com.order_service.application.checkout.event;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record InventoryReservationRequested(
        UUID eventId,
        String eventType,
        UUID checkoutId,
        UUID buyerId,
        List<Item> items
) {
    public static final String EVENT_TYPE =
            "InventoryReservationRequested";

    public InventoryReservationRequested {
        Objects.requireNonNull(eventId);
        Objects.requireNonNull(checkoutId);
        Objects.requireNonNull(buyerId);
        Objects.requireNonNull(eventType);
        Objects.requireNonNull(items);

        if (!EVENT_TYPE.equals(eventType)) {
            throw new IllegalArgumentException("Invalid event type");
        }

        items = List.copyOf(items);

        if (items.isEmpty()) {
            throw new IllegalArgumentException("Items cannot be empty");
        }

        for (Item item : items) {
            Objects.requireNonNull(item);
            Objects.requireNonNull(item.orderId());
            Objects.requireNonNull(item.variantId());

            if (item.quantity() <= 0) {
                throw new IllegalArgumentException("Invalid quantity");
            }
        }
    }

    public record Item(
            UUID orderId,
            UUID variantId,
            int quantity
    ) {}
}
