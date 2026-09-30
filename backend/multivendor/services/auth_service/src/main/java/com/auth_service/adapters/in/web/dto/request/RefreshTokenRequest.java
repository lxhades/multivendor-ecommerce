package com.auth_service.adapters.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(

        @NotBlank(message = "Refresh token không được để trống")
        String refreshToken

) {
}