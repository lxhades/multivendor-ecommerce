package com.auth_service.domain.model.vo;

public record PasswordHash(String value) {

    public PasswordHash {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Password hash must not be blank");
        }
        if(value.length() < 8) {
            throw new IllegalArgumentException("Password hash must have at least 8 characters");
        }
    }


    public static PasswordHash of(String value) {
        return new PasswordHash(value);
    }
}