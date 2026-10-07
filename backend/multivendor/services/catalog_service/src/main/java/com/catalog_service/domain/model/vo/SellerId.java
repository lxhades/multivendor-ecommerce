package com.catalog_service.domain.model.vo;

import java.util.Objects;
import java.util.UUID;

public record SellerId(UUID value) {
    public SellerId {
        Objects.requireNonNull(value, "Seller id must not be null");
    }

    public static SellerId of(UUID value) {
        return new SellerId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
