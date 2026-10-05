package com.auth_service.domain.model.entity;

import com.auth_service.domain.model.vo.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenTest {

    @Test
    void createTokenIsValidBeforeExpiration() {
        RefreshToken token = RefreshToken.create(
                UserId.of(UUID.randomUUID()),
                "refresh-token",
                Instant.now().plusSeconds(60)
        );

        assertFalse(token.isExpired());
        assertTrue(token.isValid());
        assertFalse(token.isRevoked());
    }

    @Test
    void expiredTokenIsInvalid() {
        RefreshToken token = RefreshToken.create(
                UserId.of(UUID.randomUUID()),
                "refresh-token",
                Instant.now().minusSeconds(1)
        );

        assertTrue(token.isExpired());
        assertFalse(token.isValid());
    }

    @Test
    void revokedTokenIsInvalid() {
        RefreshToken token = RefreshToken.create(
                UserId.of(UUID.randomUUID()),
                "refresh-token",
                Instant.now().plusSeconds(60)
        );

        token.revoke();

        assertTrue(token.isRevoked());
        assertFalse(token.isValid());
    }
}
