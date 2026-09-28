package com.auth_service.application.port.in;

import com.auth_service.domain.model.enumtype.UserStatus;
import com.auth_service.domain.model.vo.Email;
import com.auth_service.domain.model.vo.Phone;
import com.auth_service.domain.model.vo.UserId;

import java.util.UUID;

public record RegisterUserResult(
        String userId,
        String email,
        String phone,
        String status
) {
}

