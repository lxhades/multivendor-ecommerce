package com.auth_service.application.usecase;

import com.auth_service.application.command.UpdateAddressCommand;
import com.auth_service.application.port.in.AddressResult;
import com.auth_service.application.port.out.UserRepository;
import com.auth_service.domain.exception.AddressNotFoundException;
import com.auth_service.domain.exception.UserNotFoundException;
import com.auth_service.domain.model.aggregate.User;
import com.auth_service.domain.model.entity.Address;
import com.auth_service.domain.model.vo.Phone;
import com.auth_service.domain.model.vo.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateAddressUseCase {
    private final UserRepository userRepository;

    public UpdateAddressUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public AddressResult execute(UpdateAddressCommand command) {
        UserId userId = UserId.of(UUID.fromString(command.userId()));
        Long addressId = Long.valueOf(command.addressId());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.updateAddress(addressId, command.receiverName(), Phone.of(command.phone()),
                command.location(), command.detail(), command.isDefault());

        User saved = userRepository.save(user);
        Address address = saved.getAddresses().stream()
                .filter(item -> addressId.equals(item.getAddressId()))
                .findFirst()
                .orElseThrow(() -> new AddressNotFoundException(addressId));

        return new AddressResult(address.getAddressId(), address.getReceiverName(),
                address.getPhone().value(), address.getLocation(), address.getDetail(), address.isDefault());
    }
}
