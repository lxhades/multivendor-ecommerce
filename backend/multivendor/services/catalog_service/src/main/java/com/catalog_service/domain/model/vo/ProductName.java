package com.catalog_service.domain.model.vo;

import java.util.Objects;

public record ProductName(String value) {
    public ProductName {
        Objects.requireNonNull(value, "Product name must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Product name must not be blank");
        }
        if (value.length() > 255) {
            throw new IllegalArgumentException("Product name must not exceed 255 characters");
        }
    }
}
