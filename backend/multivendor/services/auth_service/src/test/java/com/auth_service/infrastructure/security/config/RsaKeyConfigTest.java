package com.auth_service.infrastructure.security.config;

import org.junit.jupiter.api.Test;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class RsaKeyConfigTest {

    private final RsaKeyConfig config = new RsaKeyConfig();

    @Test
    void shouldLoadPrivateKey() throws Exception {
        RSAPrivateKey key = config.rsaPrivateKey();

        assertNotNull(key);
    }

    @Test
    void shouldLoadPublicKey() throws Exception {
        RSAPublicKey key = config.rsaPublicKey();

        assertNotNull(key);
    }
}