package com.inventory_service.adapter.in.messaging.event;

import java.util.List;
import java.util.UUID;

public record InventoryReservationRequestedEvent(
        UUID eventId,
        String eventType,
        UUID checkoutId,
        UUID buyerId,
        List<Item> items
) {
    public record Item(
            UUID orderId,
            UUID variantId,
            long quantity
    ) {}
}