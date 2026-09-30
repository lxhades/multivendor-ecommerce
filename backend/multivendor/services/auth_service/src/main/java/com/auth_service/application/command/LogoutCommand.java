package com.auth_service.application.command;

public record LogoutCommand(
        String refreshToken
) {
}