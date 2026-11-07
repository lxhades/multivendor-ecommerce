package com.order_service.application.shipping.dto;

import java.time.Instant;
import java.util.UUID;

public record ShipmentResult(
        UUID id,
        UUID orderId,
        String trackingCode,
        String shippingAddress,
        String status,
        Instant createdAt,
        Instant shippedAt,
        Instant deliveredAt
) {}