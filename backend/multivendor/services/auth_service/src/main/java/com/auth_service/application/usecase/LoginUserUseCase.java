package com.auth_service.application.usecase;

import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import com.auth_service.application.command.LoginUserCommand;
import com.auth_service.application.port.in.LoginUserResult;
import com.auth_service.application.port.out.PasswordHasher;
import com.auth_service.application.port.out.RefreshTokenRepository;
import com.auth_service.application.port.out.TokenProvider;
import com.auth_service.application.port.out.UserRepository;
import com.auth_service.domain.model.aggregate.User;
import com.auth_service.domain.model.entity.RefreshToken;
import com.auth_service.domain.model.enumtype.UserStatus;
import com.auth_service.domain.model.vo.Email;
import com.auth_service.infrastructure.security.RefreshTokenGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class LoginUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenGenerator refreshTokenGenerator;

    @Value("${auth.refresh-token.expiration}")
    private long refreshTokenExpiration;

    public LoginUserUseCase(UserRepository userRepository,
                            PasswordHasher passwordHasher,
                            TokenProvider tokenProvider,
                            RefreshTokenRepository refreshTokenRepository,
                            RefreshTokenGenerator refreshTokenGenerator) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenGenerator = refreshTokenGenerator;

    }

    @Transactional
    public LoginUserResult execute(LoginUserCommand command) {
        Email email = Email.of(command.email());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DomainException(ErrorCode.EMAIL_NOT_FOUND));

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new DomainException(ErrorCode.ACCOUNT_LOCKED);
        }

        boolean passwordMatches = passwordHasher.matches(command.rawPassword(), user.getPasswordHash());
        if (!passwordMatches) {
            throw new DomainException(ErrorCode.INVALID_PASSWORD);
        }
        String accessToken = tokenProvider.generateAccessToken(user);

        String refreshTokenValue = refreshTokenGenerator.generate();

        Instant refreshTokenExpiresAt =
                Instant.now().plusSeconds(refreshTokenExpiration);

        RefreshToken refreshToken = RefreshToken.create(
                user.getUserId(),
                refreshTokenValue,
                refreshTokenExpiresAt
        );

        refreshTokenRepository.save(refreshToken);

        return new LoginUserResult(
                user.getUserId().toString(),
                accessToken,
                refreshTokenValue
        );

    }
}
