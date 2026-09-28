package com.auth_service.domain.model;



import com.auth_service.domain.model.vo.PasswordHash;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PasswordHash Value Object")
class PasswordHashTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Should create with valid hash")
        void shouldCreateValidHash() {
            PasswordHash hash = new PasswordHash("$2a$10$abcdefg");
            assertEquals("$2a$10$abcdefg", hash.value());
        }

        @Test
        @DisplayName("Should throw when null")
        void shouldThrowWhenNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> new PasswordHash(null));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @DisplayName("Should throw when blank")
        void shouldThrowWhenBlank(String value) {
            assertThrows(IllegalArgumentException.class,
                    () -> new PasswordHash(value));
        }
    }

    @Nested
    @DisplayName("equals() and hashCode()")
    class EqualsAndHashCode {

        @Test
        @DisplayName("Should be equal when same value")
        void shouldBeEqualWhenSameValue() {
            PasswordHash h1 = new PasswordHash("hash123");
            PasswordHash h2 = new PasswordHash("hash123");

            assertEquals(h1, h2);
            assertEquals(h1.hashCode(), h2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when different")
        void shouldNotBeEqualWhenDifferent() {
            PasswordHash h1 = new PasswordHash("hash123");
            PasswordHash h2 = new PasswordHash("hash456");

            assertNotEquals(h1, h2);
        }
    }
}