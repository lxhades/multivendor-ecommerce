package com.auth_service.domain.model.vo;


import java.util.regex.Pattern;

public final class Email {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final String value;


    private Email(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Email must not be null or blank");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }
        this.value = value;
    }

    public static Email of(String value) {
        return new Email(value);
    }

    public String getValue() {
        return value;
    }


    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Email other)) return false;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}