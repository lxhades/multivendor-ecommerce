package com.auth_service.domain.model;


import com.auth_service.domain.model.vo.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Email Value Object")
class EmailTest {

    @Nested
    @DisplayName("Factory method of()")
    class OfMethod {

        @Test
        @DisplayName("Should create email with valid format")
        void shouldCreateValidEmail() {
            Email email = Email.of("user@example.com");
            assertEquals("user@example.com", email.getValue());
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "a@b.com",
                "user.name@example.com",
                "user+tag@example.com",
                "user_name@example.co.uk",
                "user-name@example.com",
                "user123@test.io"
        })
        @DisplayName("Should accept various valid email formats")
        void shouldAcceptValidFormats(String value) {
            assertDoesNotThrow(() -> Email.of(value));
        }

        @Test
        @DisplayName("Should throw when email is null")
        void shouldThrowWhenNull() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> Email.of(null)
            );
            assertEquals("Email must not be null or blank", ex.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("Should throw when email is blank")
        void shouldThrowWhenBlank(String value) {
            assertThrows(IllegalArgumentException.class, () -> Email.of(value));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "abc",
                "abc@",
                "@example.com",
                "abc@def",
                "abc def@example.com",
                "abc@@example.com"
        })
        @DisplayName("Should throw when email format is invalid")
        void shouldThrowWhenInvalidFormat(String value) {
            assertThrows(IllegalArgumentException.class, () -> Email.of(value));
        }
    }

    @Nested
    @DisplayName("equals() and hashCode()")
    class EqualsAndHashCode {

        @Test
        @DisplayName("Should be equal when same value")
        void shouldBeEqualWhenSameValue() {
            Email e1 = Email.of("user@example.com");
            Email e2 = Email.of("user@example.com");

            assertEquals(e1, e2);
            assertEquals(e1.hashCode(), e2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when different value")
        void shouldNotBeEqualWhenDifferentValue() {
            Email e1 = Email.of("a@example.com");
            Email e2 = Email.of("b@example.com");

            assertNotEquals(e1, e2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            Email email = Email.of("a@example.com");
            assertNotEquals(null, email);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            Email email = Email.of("a@example.com");
            assertNotEquals("a@example.com", email);
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            Email email = Email.of("a@example.com");
            assertEquals(email, email);
        }

        @Test
        @DisplayName("Should work correctly in HashSet")
        void shouldWorkInHashSet() {
            java.util.Set<Email> set = new java.util.HashSet<>();
            set.add(Email.of("a@example.com"));
            set.add(Email.of("a@example.com"));
            set.add(Email.of("b@example.com"));

            assertEquals(2, set.size());
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringMethod {

        @Test
        @DisplayName("Should return raw value")
        void shouldReturnRawValue() {
            Email email = Email.of("user@example.com");
            assertEquals("user@example.com", email.toString());
        }
    }
}