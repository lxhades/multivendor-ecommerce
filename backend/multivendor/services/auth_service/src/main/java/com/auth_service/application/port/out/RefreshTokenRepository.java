package com.auth_service.application.port.out;

import com.auth_service.domain.model.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository {
    Optional<RefreshToken> findByToken(String token);

    RefreshToken save(RefreshToken refreshToken);

    void deleteByToken(String token);
}
