package com.auth_service.domain.model.vo;

public record Phone(String value) {

    public Phone {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Phone must not be blank");
        }
        if(value.length() < 8) {
            throw new IllegalArgumentException("Phone must have at least 8 characters");
        }
    }
    public static Phone of(String value) {
        return new Phone(value);
    }
}
