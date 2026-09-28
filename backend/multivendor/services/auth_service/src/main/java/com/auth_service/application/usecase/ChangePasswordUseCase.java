package com.auth_service.application.usecase;

import com.auth_service.application.command.ChangePasswordCommand;
import com.auth_service.application.port.out.PasswordHasher;
import com.auth_service.application.port.out.UserRepository;
import com.auth_service.domain.exception.InvalidPasswordException;
import com.auth_service.domain.exception.UserNotFoundException;
import com.auth_service.domain.model.aggregate.User;
import com.auth_service.domain.model.vo.PasswordHash;
import com.auth_service.domain.model.vo.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ChangePasswordUseCase {
    private UserRepository userRepository;
    private PasswordHasher passwordHasher;
    public ChangePasswordUseCase(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Transactional
    public void execute(ChangePasswordCommand changePasswordCommand) {
        UserId userId =UserId.of(UUID.fromString( changePasswordCommand.userId()));

        User user = userRepository.findById(userId)
                .orElseThrow(()->new UserNotFoundException(userId));
        if (passwordHasher.matches(changePasswordCommand.oldPassword(),user.getPasswordHash())){
            PasswordHash newPassword=passwordHasher.hash(changePasswordCommand.newPassword());
            user.changePassword(newPassword);
            userRepository.save(user);
        }else{
            throw new InvalidPasswordException("Mật khẩu không đúng");
        }
    }
}
