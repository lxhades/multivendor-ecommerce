package com.auth_service.domain.model.vo;

public record PasswordHash(String value) {

    public PasswordHash {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Mã băm mật khẩu không được để trống");
        }
        if (value.length() < 8) {
            throw new IllegalArgumentException("Mã băm mật khẩu phải có ít nhất 8 ký tự");
        }
    }


    public static PasswordHash of(String value) {
        return new PasswordHash(value);
    }
}