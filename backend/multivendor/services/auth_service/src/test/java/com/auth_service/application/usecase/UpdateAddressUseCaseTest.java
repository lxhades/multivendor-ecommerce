package com.auth_service.application.usecase;

import com.auth_service.application.command.UpdateAddressCommand;
import com.auth_service.application.port.out.UserRepository;
import com.auth_service.domain.exception.AddressNotFoundException;
import com.auth_service.domain.exception.UserNotFoundException;
import com.auth_service.domain.model.aggregate.User;
import com.auth_service.domain.model.entity.Address;
import com.auth_service.domain.model.enumtype.Role;
import com.auth_service.domain.model.enumtype.UserStatus;
import com.auth_service.domain.model.vo.Email;
import com.auth_service.domain.model.vo.PasswordHash;
import com.auth_service.domain.model.vo.Phone;
import com.auth_service.domain.model.vo.UserId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateAddressUseCaseTest {

    @Mock
    private UserRepository userRepository;

    private User userWithAddress() {
        return User.reconstitute(
                UserId.of(UUID.randomUUID()),
                Email.of("duc@example.com"),
                new Phone("0912345678"),
                new PasswordHash("hashed-password"),
                UserStatus.ACTIVE,
                Set.of(Role.BUYER),
                List.of(new Address(1L, "Duc", new Phone("0912345678"), "Ha Noi", "Old detail", false)),
                Instant.now(),
                Instant.now()
        );
    }

    @Test
    void shouldUpdateAddress() {
        User user = userWithAddress();
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateAddressUseCase useCase = new UpdateAddressUseCase(userRepository);

        var result = useCase.execute(new UpdateAddressCommand(
                user.getUserId().value().toString(), "1", "Nguyen Duc Anh",
                "0987654321", "Ha Noi", "New detail", true
        ));

        assertEquals(1L, result.addressId());
        assertEquals("Nguyen Duc Anh", result.receiverName());
        assertEquals("0987654321", result.phone());
        assertEquals("New detail", result.detail());
        assertTrue(result.isDefault());
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowWhenUserDoesNotExist() {
        String userId = UUID.randomUUID().toString();
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        UpdateAddressUseCase useCase = new UpdateAddressUseCase(userRepository);

        assertThrows(UserNotFoundException.class, () -> useCase.execute(
                new UpdateAddressCommand(userId, "1", "Duc", "0912345678", "Ha Noi", "Detail", false)
        ));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenAddressDoesNotBelongToUser() {
        User user = userWithAddress();
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));

        UpdateAddressUseCase useCase = new UpdateAddressUseCase(userRepository);

        assertThrows(AddressNotFoundException.class, () -> useCase.execute(
                new UpdateAddressCommand(user.getUserId().value().toString(), "999", "Duc",
                        "0912345678", "Ha Noi", "Detail", false)
        ));
        verify(userRepository, never()).save(any());
    }
}
