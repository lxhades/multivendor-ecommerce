package com.multivendor.shipping.dto;

import com.multivendor.shipping.model.Shipment;
import com.multivendor.shipping.model.ShipmentStatus;

import java.time.Instant;
import java.util.UUID;

public record ShipmentResponse(
        UUID id,
        UUID orderId,
        String trackingCode,
        String shippingAddress,
        ShipmentStatus status,
        Instant createdAt,
        Instant shippedAt,
        Instant deliveredAt
) {
    public static ShipmentResponse from(Shipment s) {
        return new ShipmentResponse(
                s.getId(),
                s.getOrderId(),
                s.getTrackingCode(),
                s.getShippingAddress(),
                s.getStatus(),
                s.getCreatedAt(),
                s.getShippedAt(),
                s.getDeliveredAt()
        );
    }
}