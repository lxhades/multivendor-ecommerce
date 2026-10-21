package com.auth_service.application.port.in;

public record LoginUserResult(
        String userId,
        String accessToken,
        String refreshToken

) {
}
