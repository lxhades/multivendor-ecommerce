package com.order_service.application.shipping.port;

import com.order_service.application.shipping.dto.ShipmentResult;

import java.util.UUID;

public interface ShippingPort {

    ShipmentResult createShipment(
            UUID orderId,
            String shippingAddress
    );

    ShipmentResult getShipment(String trackingCode);

    ShipmentResult getShipmentByOrderId(UUID orderId);
}