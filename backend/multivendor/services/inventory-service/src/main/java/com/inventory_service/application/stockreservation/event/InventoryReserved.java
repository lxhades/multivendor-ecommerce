package com.inventory_service.application.stockreservation.event;

import java.util.UUID;

public record InventoryReserved(
        UUID eventId,
        String eventType,
        UUID checkoutId,
        UUID causationEventId //ID của InventoryReservationRequested ban đầu.
) {
    public static final String EVENT_TYPE = "InventoryReserved";
}