package com.auth_service.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenGeneratorTest {

    private final RefreshTokenGenerator generator = new RefreshTokenGenerator();

    @Test
    void generatesNonBlankToken() {
        String token = generator.generate();

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void generatesDifferentTokens() {
        String first = generator.generate();
        String second = generator.generate();

        assertNotEquals(first, second);
    }
}
