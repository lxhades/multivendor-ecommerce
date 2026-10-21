package com.product_service.domain.model.vo;

import java.util.UUID;

import static com.product_service.domain.shared.Checks.required;

public record CategoryId(UUID value) {
    public CategoryId {
        required(value, "CategoryId");
    }

    public static CategoryId random() {
        return new CategoryId(UUID.randomUUID());
    }
}
