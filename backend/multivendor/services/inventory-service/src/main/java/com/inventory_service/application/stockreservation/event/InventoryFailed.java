package com.inventory_service.application.stockreservation.event;

import java.util.UUID;

public record InventoryFailed(
        UUID eventId,
        String eventType,
        UUID checkoutId,
        UUID causationEventId,
        String reason
) {
    public static final String EVENT_TYPE = "InventoryFailed";
}