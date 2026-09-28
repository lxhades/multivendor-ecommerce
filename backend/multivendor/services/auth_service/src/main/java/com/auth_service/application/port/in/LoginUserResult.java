package com.auth_service.application.port.in;

import com.auth_service.domain.model.vo.Email;
import com.auth_service.domain.model.vo.UserId;

import java.util.UUID;

public record LoginUserResult(
        String userId,
        String email

) {
}
