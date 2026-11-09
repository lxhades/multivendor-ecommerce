package com.auth_service.application.usecase;

import com.auth_service.application.port.out.UserRepository;
import com.auth_service.domain.model.enumtype.Role;
import com.auth_service.domain.model.vo.UserId;
import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RegisterAdminUseCase {
    private final UserRepository userRepository;
    public RegisterAdminUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Transactional
    public void execute(UUID userId) {
        if (!userRepository.changeRole(UserId.of(userId), Role.ADMIN)) {
            throw new DomainException(ErrorCode.USER_NOT_FOUND);
        }
    }
}
