package com.order_service.application.event;

import java.time.Instant;
import java.util.UUID;

public record OrderShipped(
        UUID eventId,
        String eventType,
        UUID orderId,
        UUID shopId,
        String shippingAddress,
        Instant occurredAt
) {
    public static final String EVENT_TYPE = "OrderShipped";

    public static OrderShipped create(
            UUID orderId,
            UUID shopId,
            String shippingAddress,
            Instant now
    ) {
        return new OrderShipped(
                UUID.randomUUID(),
                EVENT_TYPE,
                orderId,
                shopId,
                shippingAddress,
                now
        );
    }
}