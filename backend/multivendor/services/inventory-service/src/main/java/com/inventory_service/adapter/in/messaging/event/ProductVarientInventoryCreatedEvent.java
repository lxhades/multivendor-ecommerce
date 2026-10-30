package com.inventory_service.adapter.in.messaging.event;

import java.util.UUID;

public record ProductVarientInventoryCreatedEvent(
        UUID eventId,
        String eventType,
        UUID productId,
        UUID variantId,
        UUID shopId
) {
    public static final String EVENT_TYPE =
            "ProductVarientInventoryCreatedEvent";
}