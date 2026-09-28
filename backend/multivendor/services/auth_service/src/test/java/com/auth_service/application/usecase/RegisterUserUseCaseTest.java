package com.auth_service.application.usecase;

import com.auth_service.application.command.RegisterUserCommand;
import com.auth_service.application.port.in.RegisterUserResult;
import com.auth_service.application.port.out.PasswordHasher;
import com.auth_service.application.port.out.UserRepository;
import com.auth_service.domain.exception.EmailAlreadyExistsException;
import com.auth_service.domain.model.aggregate.User;
import com.auth_service.domain.model.vo.Email;
import com.auth_service.domain.model.vo.PasswordHash;
import com.auth_service.domain.model.vo.Phone;
import com.auth_service.domain.model.enumtype.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    private RegisterUserUseCase service;

    @BeforeEach
    void setUp() {
        service = new RegisterUserUseCase(passwordHasher, userRepository);
    }

    @Test
    void shouldRegisterUserSuccessfully() {

        // Arrange
        RegisterUserCommand command =
                new RegisterUserCommand(
                        "duc@example.com",
                        "0912345678",
                        "password123"
                );

        PasswordHash hashedPassword =
                new PasswordHash("hashed-password");

        when(userRepository.findByEmail(any(Email.class)))
                .thenReturn(Optional.empty());

        when(passwordHasher.hash("password123"))
                .thenReturn(hashedPassword);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        // Act
        RegisterUserResult result =
                service.execute(command);

        // Assert
        assertNotNull(result);

        assertEquals(
                "duc@example.com",
                result.email()
        );

        assertEquals(
                "0912345678",
                result.phone()
        );

        assertEquals(
                UserStatus.ACTIVE,
                result.status()
        );

        assertNotNull(result.userId());

        // Verify
        verify(userRepository)
                .findByEmail(any(Email.class));

        verify(passwordHasher)
                .hash("password123");

        verify(userRepository)
                .save(any(User.class));
    }

    @Test
    void shouldRejectWhenEmailAlreadyExists() {

        // Arrange
        RegisterUserCommand command =
                new RegisterUserCommand(
                        "duc@example.com",
                        "0912345678",
                        "password123"
                );

        User existingUser = User.register(
                Email.of("duc@example.com"),
                new Phone("0912345678"),
                new PasswordHash("existing-hash")
        );

        when(userRepository.findByEmail(any(Email.class)))
                .thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThrows(
                EmailAlreadyExistsException.class,
                () -> service.execute(command)
        );

        verify(userRepository)
                .findByEmail(any(Email.class));

        verify(passwordHasher, never())
                .hash(anyString());

        verify(userRepository, never())
                .save(any(User.class));
    }
}