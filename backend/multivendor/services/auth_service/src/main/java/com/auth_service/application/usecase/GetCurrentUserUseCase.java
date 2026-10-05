package com.auth_service.application.usecase;

import com.auth_service.application.port.in.CurrentUserResult;
import com.auth_service.application.port.out.UserRepository;
import com.auth_service.domain.exception.UserNotFoundException;
import com.auth_service.domain.model.aggregate.User;
import com.auth_service.domain.model.vo.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetCurrentUserUseCase {

    private final UserRepository userRepository;

    public GetCurrentUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public CurrentUserResult execute(String userId) {
        UserId id = UserId.of(UUID.fromString(userId));

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return new CurrentUserResult(
                user.getUserId().toString(),
                user.getEmail().getValue(),
                user.getPhone().value(),
                user.getStatus(),
                user.getRoles().stream().map(Enum::name).toList()
        );
    }
}
