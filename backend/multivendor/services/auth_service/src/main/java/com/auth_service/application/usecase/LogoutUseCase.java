package com.auth_service.application.usecase;

import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import com.auth_service.application.command.LogoutCommand;
import com.auth_service.application.port.out.RefreshTokenRepository;
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

            throw new DomainException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        refreshTokenRepository.deleteByToken(
                command.refreshToken()
        );
    }
}