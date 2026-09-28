package com.auth_service.domain.model;

import com.auth_service.domain.model.aggregate.User;
import com.auth_service.domain.model.entity.Address;
import com.auth_service.domain.model.enumtype.Role;
import com.auth_service.domain.model.enumtype.UserStatus;
import com.auth_service.domain.model.vo.Email;
import com.auth_service.domain.model.vo.PasswordHash;
import com.auth_service.domain.model.vo.Phone;
import com.auth_service.domain.model.vo.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Aggregate Root")
class UserTest {

    private Email email;
    private Phone phone;
    private PasswordHash passwordHash;

    @BeforeEach
    void setUp() {
        email = Email.of("user@example.com");
        phone = new Phone("0912345678");
        passwordHash = new PasswordHash("hashed-password");
    }

    // ============================================================
    // FACTORY: register()
    // ============================================================

    @Nested
    @DisplayName("register()")
    class RegisterMethod {

        @Test
        @DisplayName("Should register user successfully")
        void shouldRegisterSuccessfully() {
            User user = User.register(email, phone, passwordHash);

            assertNotNull(user);
            assertNotNull(user.getUserId());
            assertEquals(email, user.getEmail());
            assertEquals(phone, user.getPhone());
            assertEquals(passwordHash, user.getPasswordHash());
        }

        @Test
        @DisplayName("Should set initial status as ACTIVE")
        void shouldSetInitialStatusActive() {
            User user = User.register(email, phone, passwordHash);
            assertEquals(UserStatus.ACTIVE, user.getStatus());
        }

        @Test
        @DisplayName("Should assign BUYER role by default")
        void shouldAssignBuyerRole() {
            User user = User.register(email, phone, passwordHash);

            assertEquals(1, user.getRoles().size());
            assertTrue(user.getRoles().contains(Role.BUYER));
        }

        @Test
        @DisplayName("Should have empty addresses list")
        void shouldHaveEmptyAddresses() {
            User user = User.register(email, phone, passwordHash);
            assertTrue(user.getAddresses().isEmpty());
        }

        @Test
        @DisplayName("Should set createdAt and updatedAt")
        void shouldSetTimestamps() {
            User user = User.register(email, phone, passwordHash);

            assertNotNull(user.getCreatedAt());
            assertNotNull(user.getUpdatedAt());
            assertEquals(user.getCreatedAt(), user.getUpdatedAt());
        }

        @Test
        @DisplayName("Should generate unique UserId for each user")
        void shouldGenerateUniqueIds() {
            User u1 = User.register(email, phone, passwordHash);
            User u2 = User.register(email, phone, passwordHash);

            assertNotEquals(u1.getUserId(), u2.getUserId());
        }

        @Test
        @DisplayName("Should throw when email is null")
        void shouldThrowWhenEmailNull() {
            assertThrows(NullPointerException.class,
                    () -> User.register(null, phone, passwordHash));
        }

        @Test
        @DisplayName("Should throw when phone is null")
        void shouldThrowWhenPhoneNull() {
            assertThrows(NullPointerException.class,
                    () -> User.register(email, null, passwordHash));
        }

        @Test
        @DisplayName("Should throw when passwordHash is null")
        void shouldThrowWhenPasswordHashNull() {
            assertThrows(NullPointerException.class,
                    () -> User.register(email, phone, null));
        }
    }

    // ============================================================
    // FACTORY: reconstitute()
    // ============================================================

    @Nested
    @DisplayName("reconstitute()")
    class ReconstituteMethod {

        private UserId userId;
        private Set<Role> roles;
        private List<Address> addresses;
        private Instant createdAt;
        private Instant updatedAt;

        @BeforeEach
        void setUp() {
            userId = UserId.generate();
            roles = new HashSet<>(Set.of(Role.BUYER, Role.SELLER));
            addresses = new ArrayList<>();
            createdAt = Instant.parse("2020-01-01T00:00:00Z");
            updatedAt = Instant.parse("2024-01-01T00:00:00Z");
        }

        @Test
        @DisplayName("Should reconstitute user with all data")
        void shouldReconstituteSuccessfully() {
            User user = User.reconstitute(
                    userId, email, phone, passwordHash,
                    UserStatus.LOCKED, roles, addresses,
                    createdAt, updatedAt
            );

            assertEquals(userId, user.getUserId());
            assertEquals(email, user.getEmail());
            assertEquals(phone, user.getPhone());
            assertEquals(passwordHash, user.getPasswordHash());
            assertEquals(UserStatus.LOCKED, user.getStatus());
            assertEquals(roles, user.getRoles());
            assertEquals(createdAt, user.getCreatedAt());
            assertEquals(updatedAt, user.getUpdatedAt());
        }

        @Test
        @DisplayName("Should throw when roles is empty")
        void shouldThrowWhenRolesEmpty() {
            assertThrows(IllegalArgumentException.class,
                    () -> User.reconstitute(
                            userId, email, phone, passwordHash,
                            UserStatus.ACTIVE, Set.of(), addresses,
                            createdAt, updatedAt
                    ));
        }

        @Test
        @DisplayName("Should throw when roles is null")
        void shouldThrowWhenRolesNull() {
            assertThrows(NullPointerException.class,
                    () -> User.reconstitute(
                            userId, email, phone, passwordHash,
                            UserStatus.ACTIVE, null, addresses,
                            createdAt, updatedAt
                    ));
        }

        @Test
        @DisplayName("Should throw when status is null")
        void shouldThrowWhenStatusNull() {
            assertThrows(NullPointerException.class,
                    () -> User.reconstitute(
                            userId, email, phone, passwordHash,
                            null, roles, addresses,
                            createdAt, updatedAt
                    ));
        }

        @Test
        @DisplayName("Should throw when createdAt is null")
        void shouldThrowWhenCreatedAtNull() {
            assertThrows(NullPointerException.class,
                    () -> User.reconstitute(
                            userId, email, phone, passwordHash,
                            UserStatus.ACTIVE, roles, addresses,
                            null, updatedAt
                    ));
        }

        @Test
        @DisplayName("Should preserve timestamps from DB")
        void shouldPreserveTimestamps() {
            User user = User.reconstitute(
                    userId, email, phone, passwordHash,
                    UserStatus.ACTIVE, roles, addresses,
                    createdAt, updatedAt
            );

            assertEquals(createdAt, user.getCreatedAt());
            assertEquals(updatedAt, user.getUpdatedAt());
        }
    }

    // ============================================================
    // BEHAVIOR: changeEmail(), changePhone(), changePassword()
    // ============================================================




    @Nested
    @DisplayName("changePassword()")
    class ChangePasswordMethod {

        @Test
        @DisplayName("Should change password and update timestamp")
        void shouldChangePassword() throws InterruptedException {
            User user = User.register(email, phone, passwordHash);
            Instant before = user.getUpdatedAt();
            Thread.sleep(10);

            PasswordHash newHash = new PasswordHash("new-hash");
            user.changePassword(newHash);

            assertEquals(newHash, user.getPasswordHash());
            assertTrue(user.getUpdatedAt().isAfter(before));
        }

        @Test
        @DisplayName("Should throw when new hash is null")
        void shouldThrowWhenNull() {
            User user = User.register(email, phone, passwordHash);
            assertThrows(NullPointerException.class, () -> user.changePassword(null));
        }
    }

    // ============================================================
    // BEHAVIOR: assignRole(), revokeRole()
    // ============================================================

    @Nested
    @DisplayName("assignRole()")
    class AssignRoleMethod {

        @Test
        @DisplayName("Should assign new role successfully")
        void shouldAssignRole() {
            User user = User.register(email, phone, passwordHash);
            user.assignRole(Role.SELLER);

            assertEquals(2, user.getRoles().size());
            assertTrue(user.getRoles().contains(Role.SELLER));
            assertTrue(user.getRoles().contains(Role.BUYER));
        }

        @Test
        @DisplayName("Should not duplicate role")
        void shouldNotDuplicate() {
            User user = User.register(email, phone, passwordHash);
            user.assignRole(Role.BUYER);  // Đã có

            assertEquals(1, user.getRoles().size());
        }

        @Test
        @DisplayName("Should throw when role is null")
        void shouldThrowWhenNull() {
            User user = User.register(email, phone, passwordHash);
            assertThrows(NullPointerException.class, () -> user.assignRole(null));
        }
    }

    @Nested
    @DisplayName("revokeRole()")
    class RevokeRoleMethod {

        @Test
        @DisplayName("Should revoke role successfully when more than 1")
        void shouldRevokeRole() {
            User user = User.register(email, phone, passwordHash);
            user.assignRole(Role.SELLER);

            user.revokeRole(Role.SELLER);

            assertEquals(1, user.getRoles().size());
            assertFalse(user.getRoles().contains(Role.SELLER));
        }

        @Test
        @DisplayName("Should throw when revoke last role")
        void shouldThrowWhenLastRole() {
            User user = User.register(email, phone, passwordHash);
            // Chỉ có BUYER

            assertThrows(IllegalStateException.class,
                    () -> user.revokeRole(Role.BUYER));
        }
    }

    // ============================================================
    // BEHAVIOR: addAddress(), removeAddress(), setDefaultAddress()
    // ============================================================

//    @Nested
//    @DisplayName("addAddress()")
//    class AddAddressMethod {
//
//        @Test
//        @DisplayName("Should mark first address as default")
//        void shouldMarkFirstAsDefault() {
//            User user = User.register(email, phone, passwordHash);
//            Address addr = new Address(1L, "John", phone, "HN", "123", false);
//
//            user.addAddress(addr);
//
//            assertTrue(user.getAddresses().get(0).isDefault());
//        }
//
//        @Test
//        @DisplayName("Should not mark second address as default")
//        void shouldNotMarkSecondAsDefault() {
//            User user = User.register(email, phone, passwordHash);
//            Address addr1 = new Address(1L, "John", phone, "HN", "123", false);
//            Address addr2 = new Address(2L, "Jane", phone, "HCM", "456", false);
//
//            user.addAddress(addr1);
//            user.addAddress(addr2);
//
//            assertFalse(user.getAddresses().get(1).isDefault());
//        }
//
//        @Test
//        @DisplayName("Should throw when address is null")
//        void shouldThrowWhenNull() {
//            User user = User.register(email, phone, passwordHash);
//            assertThrows(NullPointerException.class, () -> user.addAddress(null));
//        }
//    }
//
//    @Nested
//    @DisplayName("removeAddress()")
//    class RemoveAddressMethod {
//
//        @Test
//        @DisplayName("Should remove non-default address")
//        void shouldRemoveNonDefaultAddress() {
//            User user = User.register(email, phone, passwordHash);
//            Address addr1 = new Address(1L, "John", phone, "HN", "123", false);
//            Address addr2 = new Address(2L, "Jane", phone, "HCM", "456", false);
//
//            user.addAddress(addr1);  // default
//            user.addAddress(addr2);
//
//            user.removeAddress(2L);
//
//            assertEquals(1, user.getAddresses().size());
//            assertEquals(1L, user.getAddresses().get(0).getAddressId());
//        }
//
//        @Test
//        @DisplayName("Should throw when removing default address")
//        void shouldThrowWhenRemoveDefault() {
//            User user = User.register(email, phone, passwordHash);
//            Address addr = new Address(1L, "John", phone, "HN", "123", false);
//            user.addAddress(addr);
//
//            assertThrows(IllegalStateException.class,
//                    () -> user.removeAddress(1L));
//        }
//
//        @Test
//        @DisplayName("Should throw when address not found")
//        void shouldThrowWhenNotFound() {
//            User user = User.register(email, phone, passwordHash);
//            assertThrows(IllegalArgumentException.class,
//                    () -> user.removeAddress(999L));
//        }
//    }
//
//    @Nested
//    @DisplayName("setDefaultAddress()")
//    class SetDefaultAddressMethod {
//
//        @Test
//        @DisplayName("Should switch default address")
//        void shouldSwitchDefault() {
//            User user = User.register(email, phone, passwordHash);
//            Address addr1 = new Address(1L, "John", phone, "HN", "123", false);
//            Address addr2 = new Address(2L, "Jane", phone, "HCM", "456", false);
//
//            user.addAddress(addr1);  // default = addr1
//            user.addAddress(addr2);
//
//            user.setDefaultAddress(2L);
//
//            assertFalse(user.getAddresses().get(0).isDefault());
//            assertTrue(user.getAddresses().get(1).isDefault());
//        }
//
//        @Test
//        @DisplayName("Should throw when address not found")
//        void shouldThrowWhenNotFound() {
//            User user = User.register(email, phone, passwordHash);
//            assertThrows(IllegalArgumentException.class,
//                    () -> user.setDefaultAddress(999L));
//        }
//    }

    // ============================================================
    // BEHAVIOR: lock(), activate()
    // ============================================================

    @Nested
    @DisplayName("lock()")
    class LockMethod {

        @Test
        @DisplayName("Should lock active user")
        void shouldLockActiveUser() {
            User user = User.register(email, phone, passwordHash);
            user.lock();

            assertEquals(UserStatus.LOCKED, user.getStatus());
        }

        @Test
        @DisplayName("Should throw when locking already locked user")
        void shouldThrowWhenAlreadyLocked() {
            User user = User.register(email, phone, passwordHash);
            user.lock();

            assertThrows(IllegalStateException.class, user::lock);
        }
    }

    @Nested
    @DisplayName("activate()")
    class ActivateMethod {

        @Test
        @DisplayName("Should activate locked user")
        void shouldActivateLockedUser() {
            User user = User.register(email, phone, passwordHash);
            user.lock();
            user.activate();

            assertEquals(UserStatus.ACTIVE, user.getStatus());
        }

        @Test
        @DisplayName("Should throw when activating already active user")
        void shouldThrowWhenAlreadyActive() {
            User user = User.register(email, phone, passwordHash);

            assertThrows(IllegalStateException.class, user::activate);
        }
    }

    // ============================================================
    // GETTERS: immutability check
    // ============================================================

    @Nested
    @DisplayName("Getters return immutable collections")
    class ImmutabilityTest {

        @Test
        @DisplayName("Should not modify roles through getter")
        void shouldNotModifyRoles() {
            User user = User.register(email, phone, passwordHash);

            assertThrows(UnsupportedOperationException.class,
                    () -> user.getRoles().add(Role.ADMIN));
        }

        @Test
        @DisplayName("Should not modify addresses through getter")
        void shouldNotModifyAddresses() {
            User user = User.register(email, phone, passwordHash);

            assertThrows(UnsupportedOperationException.class,
                    () -> user.getAddresses().add(null));
        }
    }

    // ============================================================
    // equals() and hashCode()
    // ============================================================

    @Nested
    @DisplayName("equals() and hashCode()")
    class EqualsAndHashCode {

        @Test
        @DisplayName("Should be equal when same userId")
        void shouldBeEqualWhenSameUserId() {
            User user = User.register(email, phone, passwordHash);

            // 2 user cùng ID
            User copy = User.reconstitute(
                    user.getUserId(), email, phone, passwordHash,
                    user.getStatus(), user.getRoles(), user.getAddresses(),
                    user.getCreatedAt(), user.getUpdatedAt()
            );

            assertEquals(user, copy);
            assertEquals(user.hashCode(), copy.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when different userId")
        void shouldNotBeEqualWhenDifferentUserId() {
            User u1 = User.register(email, phone, passwordHash);
            User u2 = User.register(email, phone, passwordHash);

            assertNotEquals(u1, u2);
        }
    }
}