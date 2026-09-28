package com.auth_service.domain.model;


import com.auth_service.domain.model.entity.Address;
import com.auth_service.domain.model.vo.Phone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Address Entity")
class AddressTest {

    private Phone validPhone;

    @BeforeEach
    void setUp() {
        validPhone = new Phone("0912345678");
    }

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Should create valid address")
        void shouldCreateValidAddress() {
            Address address = new Address(
                    1L, "John Doe", validPhone,
                    "Hanoi", "123 Main St", false
            );

            assertEquals(1L, address.getAddressId());
            assertEquals("John Doe", address.getReceiverName());
            assertEquals(validPhone, address.getPhone());
            assertEquals("Hanoi", address.getLocation());
            assertEquals("123 Main St", address.getDetail());
            assertFalse(address.isDefault());
        }

        @Test
        @DisplayName("Should throw when receiver name is null")
        void shouldThrowWhenReceiverNameNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Address(1L, null, validPhone, "HN", "123", false));
        }

        @Test
        @DisplayName("Should throw when receiver name is blank")
        void shouldThrowWhenReceiverNameBlank() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Address(1L, "   ", validPhone, "HN", "123", false));
        }

        @Test
        @DisplayName("Should throw when phone is null")
        void shouldThrowWhenPhoneNull() {
            assertThrows(NullPointerException.class,
                    () -> new Address(1L, "John", null, "HN", "123", false));
        }

        @Test
        @DisplayName("Should throw when location is null")
        void shouldThrowWhenLocationNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Address(1L, "John", validPhone, null, "123", false));
        }

        @Test
        @DisplayName("Should throw when location is blank")
        void shouldThrowWhenLocationBlank() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Address(1L, "John", validPhone, "   ", "123", false));
        }

        @Test
        @DisplayName("Should throw when detail is null")
        void shouldThrowWhenDetailNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Address(1L, "John", validPhone, "HN", null, false));
        }

        @Test
        @DisplayName("Should throw when detail is blank")
        void shouldThrowWhenDetailBlank() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Address(1L, "John", validPhone, "HN", "   ", false));
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateMethod {

        private Address address;

        @BeforeEach
        void setUp() {
            address = new Address(1L, "John", validPhone, "HN", "123", false);
        }

        @Test
        @DisplayName("Should update all fields successfully")
        void shouldUpdateSuccessfully() {
            Phone newPhone = new Phone("0987654321");

            address.update("Jane", newPhone, "HCM", "456 New St");

            assertEquals("Jane", address.getReceiverName());
            assertEquals(newPhone, address.getPhone());
            assertEquals("HCM", address.getLocation());
            assertEquals("456 New St", address.getDetail());
        }

        @Test
        @DisplayName("Should throw when new receiver name is blank")
        void shouldThrowWhenNewReceiverNameBlank() {
            assertThrows(IllegalArgumentException.class,
                    () -> address.update("  ", validPhone, "HCM", "456"));
        }

        @Test
        @DisplayName("Should throw when new phone is null")
        void shouldThrowWhenNewPhoneNull() {
            assertThrows(NullPointerException.class,
                    () -> address.update("Jane", null, "HCM", "456"));
        }

        @Test
        @DisplayName("Should throw when new location is blank")
        void shouldThrowWhenNewLocationBlank() {
            assertThrows(IllegalArgumentException.class,
                    () -> address.update("Jane", validPhone, "  ", "456"));
        }

        @Test
        @DisplayName("Should not change data when validation fails")
        void shouldNotChangeDataWhenValidationFails() {
            assertThrows(IllegalArgumentException.class,
                    () -> address.update("  ", validPhone, "HCM", "456"));

            // Dữ liệu cũ vẫn giữ nguyên
            assertEquals("John", address.getReceiverName());
            assertEquals("HN", address.getLocation());
            assertEquals("123", address.getDetail());
        }
    }

    @Nested
    @DisplayName("markAsDefault() and unmarkDefault()")
    class DefaultMethods {

        private Address address;

        @BeforeEach
        void setUp() {
            address = new Address(1L, "John", validPhone, "HN", "123", false);
        }


    }

    @Nested
    @DisplayName("equals() and hashCode()")
    class EqualsAndHashCode {

        @Test
        @DisplayName("Should be equal when same ID")
        void shouldBeEqualWhenSameId() {
            Address a1 = new Address(1L, "John", validPhone, "HN", "123", false);
            Address a2 = new Address(1L, "Jane", validPhone, "HCM", "456", true);

            assertEquals(a1, a2);  // Cùng ID
            assertEquals(a1.hashCode(), a2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when different ID")
        void shouldNotBeEqualWhenDifferentId() {
            Address a1 = new Address(1L, "John", validPhone, "HN", "123", false);
            Address a2 = new Address(2L, "John", validPhone, "HN", "123", false);

            assertNotEquals(a1, a2);
        }
    }
}
