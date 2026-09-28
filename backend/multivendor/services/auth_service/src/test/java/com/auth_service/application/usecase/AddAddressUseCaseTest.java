package com.auth_service.application.usecase;

import com.auth_service.application.command.AddAddressCommand;
import com.auth_service.application.port.out.UserRepository;
import com.auth_service.domain.model.aggregate.User;
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
class AddAddressUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void shouldAddAddress() {
        User user = User.reconstitute(
                UserId.of(UUID.randomUUID()), Email.of("duc@example.com"), new Phone("0912345678"),
                new PasswordHash("hashed-password"), UserStatus.ACTIVE, Set.of(Role.BUYER),
                List.of(), Instant.now(), Instant.now());
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = new AddAddressUseCase(userRepository).execute(new AddAddressCommand(
                user.getUserId().value().toString(), "Duc", "0987654321", "Ha Noi", "123 ABC", true));

        assertNull(result.addressId());
        assertEquals("Duc", result.receiverName());
        assertTrue(result.isDefault());
        assertEquals(1, user.getAddresses().size());
        verify(userRepository).save(user);
    }
}
