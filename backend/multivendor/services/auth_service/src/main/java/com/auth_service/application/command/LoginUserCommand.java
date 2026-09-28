package com.auth_service.application.command;

public record LoginUserCommand(
        String email,
        String rawPassword
) {
}
