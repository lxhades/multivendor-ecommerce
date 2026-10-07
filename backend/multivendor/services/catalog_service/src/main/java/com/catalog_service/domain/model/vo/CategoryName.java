package com.catalog_service.domain.model.vo;

import java.util.Objects;

public record CategoryName(String value) {
    public CategoryName {
        Objects.requireNonNull(value, "Category name must not be null");
        value = value.trim();
        if (value.isBlank()) throw new IllegalArgumentException("Category name must not be blank");
        if (value.length() > 255) throw new IllegalArgumentException("Category name must not exceed 255 characters");
    }
}
