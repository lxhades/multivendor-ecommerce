package com.auth_service.application.port.in;

import com.auth_service.domain.model.enumtype.Role;
import com.auth_service.domain.model.enumtype.UserStatus;

import java.util.List;
import java.util.Set;

public record AdminUserResult(
        String userId,
        String email,
        String phone,
        UserStatus status,
        List<String> roles
) {
}
