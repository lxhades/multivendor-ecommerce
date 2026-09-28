package com.auth_service.application.usecase;

import com.auth_service.application.command.LoginUserCommand;
import com.auth_service.application.port.in.LoginUserResult;
import com.auth_service.application.port.out.PasswordHasher;
import com.auth_service.application.port.out.UserRepository;
import com.auth_service.domain.exception.AccountLockedException;
import com.auth_service.domain.exception.EmailNotFoundException;
import com.auth_service.domain.exception.InvalidPasswordException;
import com.auth_service.domain.model.aggregate.User;
import com.auth_service.domain.model.enumtype.UserStatus;
import com.auth_service.domain.model.vo.Email;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;


    public LoginUserUseCase(UserRepository userRepository,
                             PasswordHasher passwordHasher){
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;

    }

    @Transactional(readOnly = true)
    public LoginUserResult execute(LoginUserCommand command) {
        Email email = Email.of(command.email());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EmailNotFoundException(command.email()));

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AccountLockedException("Tài khoản đã bị khóa");
        }

        boolean passwordMatches = passwordHasher.matches(command.rawPassword(), user.getPasswordHash());
        if (!passwordMatches) {
            throw new InvalidPasswordException("Mật khẩu không đúng");
        }

        return new LoginUserResult(user.getUserId().toString(), user.getEmail().toString());
    }
}
