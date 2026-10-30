package com.auth_service.application.usecase;

import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import com.auth_service.application.command.DeleteAddressCommand;
import com.auth_service.application.port.out.UserRepository;
import com.auth_service.domain.model.aggregate.User;
import com.auth_service.domain.model.vo.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteAddressUseCase {
    private final UserRepository userRepository;

    public DeleteAddressUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void execute(DeleteAddressCommand command) {
        UserId userId = UserId.of(UUID.fromString(command.userId()));
        Long addressId = Long.parseLong(command.addressId());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException(ErrorCode.USER_NOT_FOUND));
        user.deleteAddress(addressId);
        userRepository.save(user);
    }
}
