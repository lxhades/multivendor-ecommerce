
package com.inventory_service.adapter.in.messaging.event;

import java.util.List;
import java.util.UUID;

public record InventoryReserveRequestedEvent(
        UUID eventId,
        String eventType,
        UUID orderId,
        List<Item> items
) {
    public static final String EVENT_TYPE =
            "InventoryReserveRequested";

    public record Item(
            UUID variantId,
            long quantity
    ) {}
}
