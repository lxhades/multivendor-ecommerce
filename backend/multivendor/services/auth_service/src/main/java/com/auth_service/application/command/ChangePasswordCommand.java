package com.auth_service.application.command;

public record ChangePasswordCommand(
        String userId,
        String oldPassword,
        String newPassword) {
}
