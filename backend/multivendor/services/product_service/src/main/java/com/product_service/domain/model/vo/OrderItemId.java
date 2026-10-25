package com.product_service.domain.model.vo;

import java.util.UUID;

import static com.product_service.domain.shared.Checks.required;

public record OrderItemId(UUID value) {
    public OrderItemId {
        required(value, "OrderItemId");
    }

    public static OrderItemId random() {
        return new OrderItemId(UUID.randomUUID());
    }
}
