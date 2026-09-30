package com.auth_service.application.port.out;

import com.auth_service.domain.model.aggregate.User;

public interface TokenProvider {
    String generateAccessToken(User user);
}
