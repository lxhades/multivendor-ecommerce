
package com.order_service.application.checkout.event;

import java.util.List;
import java.util.UUID;

public record InventoryReservationRequested(
        UUID eventId,
        UUID checkoutId,
        UUID buyerId,
        List<Item> items
) {
    public InventoryReservationRequested {
        items = List.copyOf(items);
    }

    public record Item(
            UUID orderId,
            UUID variantId,
            int quantity
    ) {}
}
