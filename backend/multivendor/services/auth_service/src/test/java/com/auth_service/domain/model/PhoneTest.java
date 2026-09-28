package com.auth_service.domain.model;


import com.auth_service.domain.model.vo.Phone;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Phone Value Object")
class PhoneTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Should create phone with valid value")
        void shouldCreateValidPhone() {
            Phone phone = new Phone("0912345678");
            assertEquals("0912345678", phone.value());
        }

        @Test
        @DisplayName("Should throw when phone is null")
        void shouldThrowWhenNull() {
            assertThrows(IllegalArgumentException.class, () -> new Phone(null));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t"})
        @DisplayName("Should throw when phone is blank")
        void shouldThrowWhenBlank(String value) {
            assertThrows(IllegalArgumentException.class, () -> new Phone(value));
        }
    }

    @Nested
    @DisplayName("equals() and hashCode() - record auto-generated")
    class EqualsAndHashCode {

        @Test
        @DisplayName("Should be equal when same value")
        void shouldBeEqualWhenSameValue() {
            Phone p1 = new Phone("0912345678");
            Phone p2 = new Phone("0912345678");

            assertEquals(p1, p2);
            assertEquals(p1.hashCode(), p2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when different value")
        void shouldNotBeEqualWhenDifferentValue() {
            Phone p1 = new Phone("0912345678");
            Phone p2 = new Phone("0987654321");

            assertNotEquals(p1, p2);
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringMethod {

        @Test
        @DisplayName("Should return formatted string")
        void shouldReturnFormattedString() {
            Phone phone = new Phone("0912345678");
            assertEquals("Phone[value=0912345678]", phone.toString());
        }
    }
}
