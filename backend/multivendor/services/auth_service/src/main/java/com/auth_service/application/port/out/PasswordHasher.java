package com.auth_service.application.port.out;

import com.auth_service.domain.model.vo.PasswordHash;

public interface PasswordHasher {

    PasswordHash hash(String password);

    boolean matches(String password, PasswordHash passwordHash);
}
