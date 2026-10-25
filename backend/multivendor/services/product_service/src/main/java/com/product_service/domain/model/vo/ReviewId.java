package com.product_service.domain.model.vo;

import java.util.UUID;

import static com.product_service.domain.shared.Checks.required;

public record ReviewId(UUID value) {
    public ReviewId {
        required(value, "ReviewId");
    }

    public static ReviewId random() {
        return new ReviewId(UUID.randomUUID());
    }
}
