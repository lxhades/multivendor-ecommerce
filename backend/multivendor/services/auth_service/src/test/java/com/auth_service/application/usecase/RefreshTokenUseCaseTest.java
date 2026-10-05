package com.auth_service.application.usecase;

import com.auth_service.application.command.RefreshTokenCommand;
import com.auth_service.application.port.in.RefreshTokenResult;
import com.auth_service.application.port.out.RefreshTokenRepository;
import com.auth_service.application.port.out.TokenProvider;
import com.auth_service.application.port.out.UserRepository;
import com.auth_service.domain.exception.InvalidRefreshTokenException;
import com.auth_service.domain.model.aggregate.User;
import com.auth_service.domain.model.entity.RefreshToken;
import com.auth_service.domain.model.vo.Email;
import com.auth_service.domain.model.vo.PasswordHash;
import com.auth_service.domain.model.vo.Phone;
import com.auth_service.domain.model.vo.UserId;
import com.auth_service.infrastructure.security.RefreshTokenGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private RefreshTokenGenerator refreshTokenGenerator;

    private RefreshTokenUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RefreshTokenUseCase(
                refreshTokenRepository,
                userRepository,
                tokenProvider,
                refreshTokenGenerator,
                604800
        );
    }

    @Test
    void refreshRotatesTokenAndGeneratesNewAccessToken() {
        UserId userId = UserId.of(UUID.randomUUID());
        RefreshToken oldToken = RefreshToken.create(
                userId,
                "old-refresh-token",
                Instant.now().plusSeconds(600)
        );
        User user = mock(User.class);
        when(user.getUserId()).thenReturn(userId);
        when(refreshTokenRepository.findByToken("old-refresh-token"))
                .thenReturn(Optional.of(oldToken));
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(refreshTokenGenerator.generate())
                .thenReturn("new-refresh-token");
        when(tokenProvider.generateAccessToken(user))
                .thenReturn("new-access-token");

        RefreshTokenResult result = useCase.execute(
                new RefreshTokenCommand("old-refresh-token")
        );

        assertEquals("new-access-token", result.accessToken());
        assertEquals("new-refresh-token", result.refreshToken());

        verify(refreshTokenRepository).deleteByToken("old-refresh-token");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(tokenProvider).generateAccessToken(user);
    }

    @Test
    void invalidRefreshTokenIsRejected() {
        when(refreshTokenRepository.findByToken("invalid"))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidRefreshTokenException.class,
                () -> useCase.execute(new RefreshTokenCommand("invalid"))
        );

        verifyNoInteractions(userRepository, tokenProvider, refreshTokenGenerator);
    }

    @Test
    void blankRefreshTokenIsRejected() {
        assertThrows(
                InvalidRefreshTokenException.class,
                () -> useCase.execute(new RefreshTokenCommand(" "))
        );

        verifyNoInteractions(
                refreshTokenRepository,
                userRepository,
                tokenProvider,
                refreshTokenGenerator
        );
    }
}
