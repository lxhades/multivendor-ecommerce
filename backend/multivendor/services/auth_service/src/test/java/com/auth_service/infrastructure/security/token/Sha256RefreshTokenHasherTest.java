package com.auth_service.infrastructure.security.token;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Sha256RefreshTokenHasherTest {

    private final Sha256RefreshTokenHasher hasher =
            new Sha256RefreshTokenHasher();

    @Test
    void sameTokenProducesSameHash() {
        String token = "refresh-token-example";

        assertEquals(hasher.hash(token), hasher.hash(token));
    }

    @Test
    void differentTokensProduceDifferentHashes() {
        assertNotEquals(
                hasher.hash("token-1"),
                hasher.hash("token-2")
        );
    }

    @Test
    void blankTokenIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> hasher.hash(" ")
        );
    }
}
