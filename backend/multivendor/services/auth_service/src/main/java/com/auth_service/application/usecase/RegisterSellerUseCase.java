package com.auth_service.application.usecase;

import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import com.auth_service.application.port.out.UserRepository;
import com.auth_service.domain.model.aggregate.User;
import com.auth_service.domain.model.enumtype.Role;
import com.auth_service.domain.model.vo.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RegisterSellerUseCase {
    private final UserRepository userRepository;

    public RegisterSellerUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void RegisterSeller(UUID userId) {

        User user = userRepository.findById(
                        UserId.of(userId))
                .orElseThrow(() -> new DomainException(ErrorCode.USER_NOT_FOUND));
        user.assignRole(Role.SELLER);
        userRepository.save(user);
    }
}
