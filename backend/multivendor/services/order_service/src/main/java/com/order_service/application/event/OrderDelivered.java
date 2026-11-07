package com.order_service.application.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderDelivered(
        UUID eventId,
        String eventType,
        UUID orderId,
        UUID shopId,
        Instant deliveredAt,
        List<Item> items
) {
    public static final String EVENT_TYPE = "OrderDelivered";

    public record Item(UUID productId, UUID variantId, int quantity) {}
}
