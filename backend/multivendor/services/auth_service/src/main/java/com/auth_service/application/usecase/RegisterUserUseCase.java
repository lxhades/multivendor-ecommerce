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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterUserUseCase {

    private final PasswordHasher passwordHasher;
    private final UserRepository userRepository;

    public RegisterUserUseCase(PasswordHasher passwordHasher, UserRepository userRepository) {
        this.passwordHasher = passwordHasher;
        this.userRepository = userRepository;
    }

    @Transactional
    public RegisterUserResult execute(RegisterUserCommand command) {
        Email email = Email.of(command.email());
        Phone phone = new Phone(command.phone());

        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException(command.email());
        }

        PasswordHash passwordHash = passwordHasher.hash(command.password());
        User newUser = User.register(email, phone, passwordHash);

        User savedUser;
        try {
            savedUser = userRepository.save(newUser);
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyExistsException(command.email());
        }

        return new RegisterUserResult(
                savedUser.getUserId().value().toString(),
                savedUser.getEmail().getValue(),
                savedUser.getPhone().value(),
                savedUser.getStatus().toString()
        );
    }
}
