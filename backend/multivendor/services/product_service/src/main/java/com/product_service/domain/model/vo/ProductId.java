package com.product_service.domain.model.vo;

import java.util.UUID;

import static com.product_service.domain.shared.Checks.required;

public record ProductId(UUID value) {
    public ProductId {
        required(value, "ProductId");
    }

    public static ProductId random() {
        return new ProductId(UUID.randomUUID());
    }
}
