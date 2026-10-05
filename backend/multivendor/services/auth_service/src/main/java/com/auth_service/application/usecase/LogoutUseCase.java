package com.auth_service.application.usecase;

import com.auth_service.application.command.LogoutCommand;
import com.auth_service.application.port.out.RefreshTokenRepository;
import com.auth_service.domain.exception.InvalidRefreshTokenException;
import org.springframework.stereotype.Service;

@Service
public class LogoutUseCase {

    private final RefreshTokenRepository refreshTokenRepository;

    public LogoutUseCase(
            RefreshTokenRepository refreshTokenRepository
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public void execute(LogoutCommand command) {

        if (command == null
                || command.refreshToken() == null
                || command.refreshToken().isBlank()) {

            throw new InvalidRefreshTokenException();
        }

        refreshTokenRepository.deleteByToken(
                command.refreshToken()
        );
    }
}