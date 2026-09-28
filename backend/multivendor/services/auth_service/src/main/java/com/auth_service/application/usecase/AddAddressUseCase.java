package com.auth_service.application.usecase;

import com.auth_service.application.command.AddAddressCommand;
import com.auth_service.application.port.in.AddressResult;
import com.auth_service.application.port.out.UserRepository;
import com.auth_service.domain.exception.UserNotFoundException;
import com.auth_service.domain.model.aggregate.User;
import com.auth_service.domain.model.entity.Address;
import com.auth_service.domain.model.vo.Phone;
import com.auth_service.domain.model.vo.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AddAddressUseCase {
    private final UserRepository userRepository;

    public AddAddressUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public AddressResult execute(AddAddressCommand command) {
        UserId userId = UserId.of(UUID.fromString(command.userId()));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Address address = new Address(
                null,
                command.receiverName(),
                Phone.of(command.phone()),
                command.location(),
                command.detail(),
                command.isDefault()
        );

        user.addAddress(address);
        User saved = userRepository.save(user);
        Address savedAddress = saved.getAddresses().get(saved.getAddresses().size() - 1);
        return toResult(savedAddress);
    }

    private AddressResult toResult(Address address) {
        return new AddressResult(address.getAddressId(), address.getReceiverName(),
                address.getPhone().value(), address.getLocation(), address.getDetail(), address.isDefault());
    }
}
