package com.order_service.application.event;

import java.util.UUID;

public record InventoryReleaseRequested(
        UUID eventId,
        String eventType,
        UUID orderId
) {
    public static final String EVENT_TYPE =
            "InventoryReleaseRequested";
}