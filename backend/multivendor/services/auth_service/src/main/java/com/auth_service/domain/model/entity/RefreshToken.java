package com.auth_service.domain.model.entity;

import com.auth_service.domain.model.vo.UserId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class RefreshToken {

    private final UUID id;
    private final UserId userId;
    private final String token;
    private final Instant expiresAt;
    private boolean revoked;

    private RefreshToken(
            UUID id,
            UserId userId,
            String token,
            Instant expiresAt,
            boolean revoked
    ) {
        this.id = Objects.requireNonNull(id, "Mã định danh làm mới phiên đăng nhập không được để trống");
        this.userId = Objects.requireNonNull(userId, "Mã người dùng không được để trống");
        this.token = Objects.requireNonNull(token, "Mã xác thực không được để trống");
        this.expiresAt = Objects.requireNonNull(expiresAt, "Thời điểm hết hạn không được để trống");
        this.revoked = revoked;

        if (token.isBlank()) {
            throw new IllegalArgumentException("Mã xác thực không được để trống");
        }
    }

    public static RefreshToken create(
            UserId userId,
            String token,
            Instant expiresAt
    ) {
        return new RefreshToken(
                UUID.randomUUID(),
                userId,
                token,
                expiresAt,
                false
        );
    }

    public static RefreshToken reconstitute(
            UUID id,
            UserId userId,
            String token,
            Instant expiresAt,
            boolean revoked
    ) {
        return new RefreshToken(
                id,
                userId,
                token,
                expiresAt,
                revoked
        );
    }

    public boolean isExpired() {
        return !Instant.now().isBefore(expiresAt);
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }

    public void revoke() {
        this.revoked = true;
    }

    public UUID getId() {
        return id;
    }

    public UserId getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }
}