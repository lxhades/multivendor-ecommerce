package com.auth_service.domain.exception;

import com.auth_service.domain.model.vo.UserId;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(UserId userId) {
        super("User not found: " + userId);
    }
}
