package com.multivendor.shipping.dto;

import java.time.Instant;
import java.util.UUID;

public record ShippingStatusChanged(
        UUID eventId,
        String eventType,
        UUID orderId,
        String status,
        Instant occurredAt
) {
    public static final String EVENT_TYPE = "ShippingStatusChanged";

    public static ShippingStatusChanged delivered(
            UUID orderId,
            Instant now
    ) {
        return new ShippingStatusChanged(
                UUID.randomUUID(),
                EVENT_TYPE,
                orderId,
                "DELIVERED",
                now
        );
    }
}