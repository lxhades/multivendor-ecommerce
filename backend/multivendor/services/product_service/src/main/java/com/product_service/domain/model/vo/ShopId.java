package com.product_service.domain.model.vo;

import java.util.UUID;

import static com.product_service.domain.shared.Checks.required;

public record ShopId(UUID value) {
    public ShopId {
        required(value, "ShopId");
    }

    public static ShopId random() {
        return new ShopId(UUID.randomUUID());
    }
}
