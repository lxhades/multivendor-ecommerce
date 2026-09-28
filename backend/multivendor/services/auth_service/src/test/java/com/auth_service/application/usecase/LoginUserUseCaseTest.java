package com.auth_service.application.usecase;

import com.auth_service.application.command.LoginUserCommand;
import com.auth_service.application.port.in.LoginUserResult;
import com.auth_service.application.port.out.PasswordHasher;
import com.auth_service.application.port.out.UserRepository;
import com.auth_service.domain.exception.AccountLockedException;
import com.auth_service.domain.exception.EmailNotFoundException;
import com.auth_service.domain.exception.InvalidPasswordException;
import com.auth_service.domain.model.aggregate.User;
import com.auth_service.domain.model.enumtype.Role;
import com.auth_service.domain.model.enumtype.UserStatus;
import com.auth_service.domain.model.vo.Email;
import com.auth_service.domain.model.vo.PasswordHash;
import com.auth_service.domain.model.vo.Phone;
import com.auth_service.domain.model.vo.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;


    private LoginUserUseCase service;

    @BeforeEach
    void setUp() {
        service = new LoginUserUseCase(userRepository, passwordHasher);
    }

    private User userWithStatus(UserStatus status) {
        return User.reconstitute(
                UserId.of(UUID.randomUUID()),
                Email.of("duc@example.com"),
                new Phone("0912345678"),
                new PasswordHash("hashed-password"),
                status,
                Set.of(Role.BUYER),
                List.of(),
                Instant.now(),
                Instant.now()
        );
    }

    @Test
    void shouldLoginSuccessfullyAndReturnToken() {
        User user = userWithStatus(UserStatus.ACTIVE);
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
        when(passwordHasher.matches(eq("password123"), any(PasswordHash.class))).thenReturn(true);


        LoginUserResult result = service.execute(new LoginUserCommand("duc@example.com", "password123"));

        assertEquals(user.getUserId().value(), result.userId());
        assertEquals("duc@example.com", result.email());

    }

    @Test
    void shouldRejectWrongPassword() {
        User user = userWithStatus(UserStatus.ACTIVE);
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
        when(passwordHasher.matches(eq("wrong-password"), any(PasswordHash.class))).thenReturn(false);

        assertThrows(InvalidPasswordException.class,
                () -> service.execute(new LoginUserCommand("duc@example.com", "wrong-password")));
    }

    @Test
    void shouldRejectUnknownEmail() {
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());

        assertThrows(EmailNotFoundException.class,
                () -> service.execute(new LoginUserCommand("nobody@example.com", "password123")));

        verify(passwordHasher, never()).matches(any(), any());
    }

    @Test
    void shouldRejectLockedAccount() {
        User user = userWithStatus(UserStatus.LOCKED);
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));

        assertThrows(AccountLockedException.class,
                () -> service.execute(new LoginUserCommand("duc@example.com", "password123")));

        verify(passwordHasher, never()).matches(any(), any());
    }
}
