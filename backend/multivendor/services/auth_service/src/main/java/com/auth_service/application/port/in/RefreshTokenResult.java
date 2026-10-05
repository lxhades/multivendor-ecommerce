package com.auth_service.application.port.in;

public record RefreshTokenResult(
        String accessToken,
        String refreshToken
) {
}