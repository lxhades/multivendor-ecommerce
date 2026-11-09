package com.auth_service.application.port.in;

import com.auth_service.domain.model.enumtype.UserStatus;

import com.auth_service.domain.model.enumtype.Role;

public record AdminUserResult(
        String userId,
        String email,
        String phone,
        UserStatus status,
        Role role
) {
}
