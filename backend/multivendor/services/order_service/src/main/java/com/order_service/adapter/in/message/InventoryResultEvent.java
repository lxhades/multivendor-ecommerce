package com.order_service.adapter.in.message;

import java.util.UUID;

public record InventoryResultEvent(
        UUID eventId,
        String eventType,
        UUID checkoutId,
        UUID causationEventId,
        String reason
) {}