package com.auth_service.application.usecase;

import com.auth_service.application.port.out.UserRepository;
import com.auth_service.domain.exception.UserNotFoundException;
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
                .orElseThrow(() -> new UserNotFoundException(UserId.of(userId)));
        user.assignRole(Role.SELLER);
        userRepository.save(user);
    }
}
