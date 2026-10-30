package com.auth_service.application.usecase;

import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import com.auth_service.application.command.RefreshTokenCommand;
import com.auth_service.application.port.in.RefreshTokenResult;
import com.auth_service.application.port.out.RefreshTokenRepository;
import com.auth_service.application.port.out.TokenProvider;
import com.auth_service.application.port.out.UserRepository;
import com.auth_service.domain.model.aggregate.User;
import com.auth_service.domain.model.entity.RefreshToken;
import com.auth_service.infrastructure.security.RefreshTokenGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class RefreshTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final RefreshTokenGenerator refreshTokenGenerator;

    private final long refreshTokenExpiration;

    public RefreshTokenUseCase(
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            TokenProvider tokenProvider,
            RefreshTokenGenerator refreshTokenGenerator,
            @Value("${auth.refresh-token.expiration}") long refreshTokenExpiration
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    @Transactional
    public RefreshTokenResult execute(RefreshTokenCommand command) {

        if (command == null
                || command.refreshToken() == null
                || command.refreshToken().isBlank()) {

            throw new DomainException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        RefreshToken oldRefreshToken =
                refreshTokenRepository
                        .findByToken(command.refreshToken())
                        .orElseThrow(() -> new DomainException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (!oldRefreshToken.isValid()) {
            throw new DomainException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        User user = userRepository
                .findById(oldRefreshToken.getUserId())
                .orElseThrow(() -> new DomainException(ErrorCode.USER_NOT_FOUND));

        refreshTokenRepository.deleteByToken(
                oldRefreshToken.getToken()
        );


        String newRefreshTokenValue =
                refreshTokenGenerator.generate();

        Instant newExpiresAt =
                Instant.now().plusSeconds(refreshTokenExpiration);

        RefreshToken newRefreshToken =
                RefreshToken.create(
                        user.getUserId(),
                        newRefreshTokenValue,
                        newExpiresAt
                );


        refreshTokenRepository.save(newRefreshToken);

        String newAccessToken =
                tokenProvider.generateAccessToken(user);


        return new RefreshTokenResult(
                newAccessToken,
                newRefreshTokenValue
        );
    }
}