package com.product_service.domain.model.vo;

import java.util.UUID;

import static com.product_service.domain.shared.Checks.required;

public record SellerId(UUID value) {
    public SellerId {
        required(value, "SellerId");
    }

    public static SellerId random() {
        return new SellerId(UUID.randomUUID());
    }
}
