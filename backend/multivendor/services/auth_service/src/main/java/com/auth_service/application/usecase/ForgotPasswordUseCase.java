package com.auth_service.application.usecase;

import com.auth_service.application.command.ForgotPasswordCommand;
import com.auth_service.application.port.out.*;
import com.auth_service.domain.exception.EmailNotFoundException;
import com.auth_service.domain.model.aggregate.User;
import com.auth_service.domain.model.vo.Email;
import com.auth_service.domain.model.vo.PasswordHash;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
public class ForgotPasswordUseCase {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final NotificationSender notificationSender;
    public ForgotPasswordUseCase(UserRepository userRepository, PasswordHasher passwordHasher,NotificationSender notificationSender) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.notificationSender = notificationSender;
    }

    @Transactional
    public void execute(ForgotPasswordCommand command){
        User user = userRepository.findByEmail(Email.of(command.email()))
                .orElseThrow(() -> new EmailNotFoundException(command.email()));

        String newPassword = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        PasswordHash newPasswordHash=passwordHasher.hash(newPassword);
        user.changePassword(newPasswordHash);
        userRepository.save(user);
        notificationSender.send(command.email(),newPassword);


    }

}