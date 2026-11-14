package com.auth_service.adapters.in.web.dto.request;

import jakarta.validation.constraints.*;

public record AdminUpdateUserRequest(
        @NotBlank @Email @Size(max = 100) String email,
        @NotBlank @Pattern(regexp = "^0[0-9]{9}$") String phone
) {
    public AdminUpdateUserRequest {
        email = email == null ? null : email.trim();
        phone = phone == null ? null : phone.trim();
    }
}
