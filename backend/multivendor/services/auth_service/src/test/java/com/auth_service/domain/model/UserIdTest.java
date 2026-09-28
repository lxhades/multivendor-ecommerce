package com.auth_service.domain.model;


import com.auth_service.domain.model.vo.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserId Value Object")
class UserIdTest {

    @Nested
    @DisplayName("generate()")
    class GenerateMethod {

        @Test
        @DisplayName("Should generate non-null UserId")
        void shouldGenerateNonNull() {
            UserId userId = UserId.generate();
            assertNotNull(userId);
            assertNotNull(userId.value());
        }

        @Test
        @DisplayName("Should generate unique UserIds")
        void shouldGenerateUnique() {
            UserId id1 = UserId.generate();
            UserId id2 = UserId.generate();
            assertNotEquals(id1, id2);
        }
    }

    @Nested
    @DisplayName("of()")
    class OfMethod {

        @Test
        @DisplayName("Should create UserId from UUID")
        void shouldCreateFromUuid() {
            UUID uuid = UUID.randomUUID();
            UserId userId = UserId.of(uuid);
            assertEquals(uuid, userId.value());
        }

        @Test
        @DisplayName("Should throw when UUID is null")
        void shouldThrowWhenNull() {
            assertThrows(NullPointerException.class,
                    () -> UserId.of(null));
        }
    }

    @Nested
    @DisplayName("equals() and hashCode()")
    class EqualsAndHashCode {

        @Test
        @DisplayName("Should be equal when same UUID")
        void shouldBeEqualWhenSameUuid() {
            UUID uuid = UUID.randomUUID();
            UserId id1 = UserId.of(uuid);
            UserId id2 = UserId.of(uuid);

            assertEquals(id1, id2);
            assertEquals(id1.hashCode(), id2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when different UUID")
        void shouldNotBeEqualWhenDifferentUuid() {
            UserId id1 = UserId.generate();
            UserId id2 = UserId.generate();

            assertNotEquals(id1, id2);
        }

        @Test
        @DisplayName("Should work in HashSet")
        void shouldWorkInHashSet() {
            UUID uuid = UUID.randomUUID();
            Set<UserId> set = new HashSet<>();
            set.add(UserId.of(uuid));
            set.add(UserId.of(uuid));

            assertEquals(1, set.size());
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringMethod {

        @Test
        @DisplayName("Should return UUID string")
        void shouldReturnUuidString() {
            UUID uuid = UUID.randomUUID();
            UserId userId = UserId.of(uuid);
            assertEquals(uuid.toString(), userId.toString());
        }
    }
}
