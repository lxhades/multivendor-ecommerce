package com.multivendor.shipping.dto;

import java.time.Instant;
import java.util.UUID;

public record OrderShippedEvent(
        UUID eventId,
        String eventType,
        UUID orderId,
        UUID shopId,
        String shippingAddress,
        Instant occurredAt
) {}