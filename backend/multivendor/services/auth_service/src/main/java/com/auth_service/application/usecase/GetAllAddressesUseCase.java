package com.auth_service.application.usecase;

import com.auth_service.application.port.in.AddressResult;
import com.auth_service.application.port.out.UserRepository;
import com.auth_service.application.query.GetAllAdressesQuery;
import com.auth_service.domain.exception.UserNotFoundException;
import com.auth_service.domain.model.aggregate.User;
import com.auth_service.domain.model.entity.Address;
import com.auth_service.domain.model.vo.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GetAllAddressesUseCase {

    private final UserRepository userRepository;

    public GetAllAddressesUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AddressResult> execute(GetAllAdressesQuery query) {
        UserId userId = UserId.of(UUID.fromString(query.userId()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return user.getAddresses().stream()
                .map(this::toAddressResult)
                .toList();
    }

    private AddressResult toAddressResult(Address address) {
        return new AddressResult(
                address.getAddressId(),
                address.getReceiverName(),
                address.getPhone().value(),
                address.getLocation(),
                address.getDetail(),
                address.isDefault()
        );
    }
}