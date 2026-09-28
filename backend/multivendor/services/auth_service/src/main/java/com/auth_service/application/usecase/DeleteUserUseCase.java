package com.auth_service.application.usecase;

import com.auth_service.application.command.DeleteUserCommand;
import com.auth_service.application.port.out.UserRepository;
import com.auth_service.domain.exception.UserNotFoundException;
import com.auth_service.domain.model.vo.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteUserUseCase {
    private final UserRepository userRepository;

    public DeleteUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void execute(DeleteUserCommand command) {
        UserId userId = UserId.of(UUID.fromString(command.userId()));
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        userRepository.deleteById(userId);
    }
}
