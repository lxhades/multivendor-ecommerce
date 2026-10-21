package com.auth_service.application.port.in;

public record RegisterUserResult(
        String userId,
        String email,
        String phone,
        String status
) {
}

