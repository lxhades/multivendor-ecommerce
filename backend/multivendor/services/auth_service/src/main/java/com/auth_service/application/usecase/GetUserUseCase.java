package com.auth_service.application.usecase;

import com.auth_service.application.port.in.AdminUserResult;
import com.auth_service.application.port.out.UserRepository;
import com.auth_service.application.query.GetUserQuery;
import com.auth_service.domain.exception.UserNotFoundException;
import com.auth_service.domain.model.aggregate.User;
import com.auth_service.domain.model.vo.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetUserUseCase {
    private final UserRepository userRepository;

    public GetUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public AdminUserResult execute(GetUserQuery query) {
        UserId userId = UserId.of(UUID.fromString(query.userId()));
        return userRepository.findById(userId)
                .map(this::toResult)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }
    private AdminUserResult toResult(User user) {
        if(user == null) {
            return null;
        }
        AdminUserResult adminUserResult = new AdminUserResult(
                user.getUserId().toString(),
                user.getEmail().getValue(),
                user.getPhone().value(),
                user.getStatus(),
                user.getRoles().stream()
                        .map(Enum::name)
                        .toList()

        );
        return adminUserResult;
    }
}
