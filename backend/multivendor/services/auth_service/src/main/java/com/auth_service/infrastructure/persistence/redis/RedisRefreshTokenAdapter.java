package com.auth_service.infrastructure.persistence.redis;

import com.auth_service.application.port.out.RefreshTokenHasher;
import com.auth_service.application.port.out.RefreshTokenRepository;
import com.auth_service.domain.model.entity.RefreshToken;
import com.auth_service.domain.model.vo.UserId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RedisRefreshTokenAdapter implements RefreshTokenRepository {

    private static final String KEY_PREFIX = "refresh_token:";

    private final StringRedisTemplate redisTemplate;
    private final RefreshTokenHasher refreshTokenHasher;

    public RedisRefreshTokenAdapter(
            StringRedisTemplate redisTemplate,
            RefreshTokenHasher refreshTokenHasher
    ) {
        this.redisTemplate = redisTemplate;
        this.refreshTokenHasher = refreshTokenHasher;
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        String tokenHash = refreshTokenHasher.hash(refreshToken.getToken());
        String key = KEY_PREFIX + tokenHash;

        long ttlSeconds = Duration.between(
                Instant.now(),
                refreshToken.getExpiresAt()
        ).getSeconds();

        if (ttlSeconds <= 0) {
            throw new IllegalArgumentException("Refresh token must not be expired");
        }

        redisTemplate.opsForValue().set(
                key,
                refreshToken.getUserId().toString(),
                Duration.ofSeconds(ttlSeconds)
        );

        return refreshToken;
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        String tokenHash = refreshTokenHasher.hash(token);
        String key = KEY_PREFIX + tokenHash;

        String userIdValue = redisTemplate.opsForValue().get(key);
        if (userIdValue == null) {
            return Optional.empty();
        }

        Long ttl = redisTemplate.getExpire(key);
        if (ttl == null || ttl <= 0) {
            return Optional.empty();
        }

        UserId userId = UserId.of(UUID.fromString(userIdValue));
        Instant expiresAt = Instant.now().plusSeconds(ttl);

        return Optional.of(
                RefreshToken.reconstitute(
                        UUID.randomUUID(),
                        userId,
                        token,
                        expiresAt,
                        false
                )
        );
    }

    @Override
    public void deleteByToken(String token) {
        String tokenHash = refreshTokenHasher.hash(token);
        redisTemplate.delete(KEY_PREFIX + tokenHash);
    }
}
