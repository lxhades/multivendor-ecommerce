package com.auth_service.application.command;

public record RegisterUserCommand(
        String email,
        String phone,
        String password
) {
}
