package com.auth_service.application.command;

public record RefreshTokenCommand(
        String refreshToken
) {
}