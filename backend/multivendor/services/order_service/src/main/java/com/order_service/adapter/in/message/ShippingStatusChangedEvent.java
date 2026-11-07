package com.order_service.adapter.in.message;

import java.time.Instant;
import java.util.UUID;

public record ShippingStatusChangedEvent(
        UUID eventId,
        String eventType,
        UUID orderId,
        String status,
        Instant occurredAt
) {}