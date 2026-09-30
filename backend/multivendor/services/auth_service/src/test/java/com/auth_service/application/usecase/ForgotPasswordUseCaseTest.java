package com.auth_service.application.usecase;

import com.auth_service.application.command.ForgotPasswordCommand;
import com.auth_service.application.port.out.NotificationSender;
import com.auth_service.application.port.out.PasswordHasher;
import com.auth_service.application.port.out.UserRepository;
import com.auth_service.domain.exception.EmailNotFoundException;
import com.auth_service.domain.model.aggregate.User;
import com.auth_service.domain.model.vo.Email;
import com.auth_service.domain.model.vo.PasswordHash;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ForgotPasswordUseCaseTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private NotificationSender notificationSender;

    @InjectMocks
    private ForgotPasswordUseCase useCase;

    private String email;
    private User user;

    @BeforeEach
    void setUp() {
        email= "ducanh957z@gmail.com";
        user = mock(User.class);
    }
    @Test
    @DisplayName("User tồn tại → đổi password, lưu user, gửi mail")
    void execute_whenUserExists_shouldChangePasswordAndSendEmail() {

        ForgotPasswordCommand command = new ForgotPasswordCommand(email);
        PasswordHash hashed = new PasswordHash("$2a$10$hashedvalue");

        when(userRepository.findByEmail(Email.of(email))).thenReturn(Optional.of(user));
        when(passwordHasher.hash(anyString())).thenReturn(hashed);

        // When
        useCase.execute(command);

        // Then
        verify(userRepository).findByEmail(Email.of(email));
        verify(user).changePassword(hashed);
        verify(userRepository).save(user);
        verify(notificationSender).send(eq("ducanh957z@gmail.com"), anyString());
    }
    @Test
    @DisplayName("Password mới phải được hash trước khi lưu (không lưu plain text)")
    void execute_shouldHashPasswordBeforeSaving() {
        ForgotPasswordCommand command = new ForgotPasswordCommand(email);
        PasswordHash hashed = new PasswordHash("$2a$10$hashedvalue");

        when(userRepository.findByEmail(Email.of(email))).thenReturn(Optional.of(user));
        when(passwordHasher.hash(anyString())).thenReturn(hashed);

        useCase.execute(command);

        // Verify rằng changePassword được gọi với PasswordHash, không phải String
        ArgumentCaptor<PasswordHash> captor = ArgumentCaptor.forClass(PasswordHash.class);
        verify(user).changePassword(captor.capture());

        assertThat(captor.getValue()).isEqualTo(hashed);
        assertThat(captor.getValue().value()).isNotBlank();
    }

    @Test
    @DisplayName("Password gửi qua mail là password gốc, không phải hash")
    void execute_shouldSendRawPasswordNotHashedOne() {
        ForgotPasswordCommand command = new ForgotPasswordCommand(email);
        PasswordHash hashed = new PasswordHash("$2a$10$hashedvalue");

        when(userRepository.findByEmail(Email.of(email))).thenReturn(Optional.of(user));
        when(passwordHasher.hash(anyString())).thenReturn(hashed);

        useCase.execute(command);

        ArgumentCaptor<String> mailContent = ArgumentCaptor.forClass(String.class);
        verify(notificationSender).send(anyString(), mailContent.capture());

        String sentPassword = mailContent.getValue();
        assertThat(sentPassword).isNotEqualTo(hashed.value());
        assertThat(sentPassword).isNotBlank();
    }
    @Test
    @DisplayName("User không tồn tại → throw EmailNotFoundException")
    void execute_whenUserNotFound_shouldThrowEmailNotFoundException() {
        ForgotPasswordCommand command = new ForgotPasswordCommand(email);

        when(userRepository.findByEmail(Email.of(email))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(EmailNotFoundException.class)
                .hasMessageContaining("ducanh957z@gmail.com");

        // Verify KHÔNG có side effect nào xảy ra
        verify(passwordHasher, never()).hash(anyString());
        verify(userRepository, never()).save(any());
        verify(notificationSender, never()).send(anyString(), anyString());
    }
}
