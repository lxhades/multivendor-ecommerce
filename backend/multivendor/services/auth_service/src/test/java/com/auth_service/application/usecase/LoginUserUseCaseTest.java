package com.auth_service.application.usecase;


import com.auth_service.application.command.LoginUserCommand;
import com.auth_service.application.port.in.LoginUserResult;
import com.auth_service.application.port.out.PasswordHasher;
import com.auth_service.application.port.out.TokenProvider;
import com.auth_service.application.port.out.UserRepository;
import com.auth_service.domain.model.aggregate.User;
import com.auth_service.domain.model.vo.Email;
import com.auth_service.domain.model.vo.PasswordHash;
import com.auth_service.domain.model.vo.Phone;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private TokenProvider tokenProvider;

    @Test
    void shouldLoginSuccessfully() {

        LoginUserUseCase useCase =
                new LoginUserUseCase(
                        userRepository,
                        passwordHasher,
                        tokenProvider
                );

        User user = User.register(
                Email.of("test@gmail.com"),
                Phone.of("0123456789"),
                new PasswordHash("hashed-password")
        );

        when(userRepository.findByEmail(
                Email.of("test@gmail.com")
        )).thenReturn(Optional.of(user));

        when(passwordHasher.matches(
                "123456",
                PasswordHash.of("hashed-password")
        )).thenReturn(true);

        when(tokenProvider.generateAccessToken(user))
                .thenReturn("jwt-token");

        LoginUserResult result = useCase.execute(
                new LoginUserCommand(
                        "test@gmail.com",
                        "123456"
                )
        );

        assertEquals("jwt-token", result.accessToken());

        verify(userRepository).findByEmail(
                Email.of("test@gmail.com")
        );

        verify(passwordHasher).matches(
                "123456",
                PasswordHash.of("hashed-password")
        );

        verify(tokenProvider).generateAccessToken(user);
    }
}