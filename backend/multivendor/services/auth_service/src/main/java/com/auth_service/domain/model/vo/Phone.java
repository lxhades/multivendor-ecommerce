package com.auth_service.domain.model.vo;

public record Phone(String value) {

    public Phone {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Số điện thoại không được để trống");
        }
        if (value.length() < 8) {
            throw new IllegalArgumentException("Số điện thoại phải có ít nhất 8 ký tự");
        }
    }

    public static Phone of(String value) {
        return new Phone(value);
    }
}
