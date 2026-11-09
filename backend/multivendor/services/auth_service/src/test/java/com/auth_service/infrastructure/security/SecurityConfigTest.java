package com.auth_service.infrastructure.security;

import com.auth_service.adapters.in.web.controller.JwkSetController;
import com.nimbusds.jose.jwk.JWKSet;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtValidationException;

import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SecurityConfigTest {
    @Test
    void issuedTokenMatchesPublishedKeyAndDecoderChecksIssuer() throws Exception {
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        var pair = generator.generateKeyPair();
        var publicKey = (RSAPublicKey) pair.getPublic();
        var config = new SecurityConfig();
        var source = config.rsaKey(publicKey, (RSAPrivateKey) pair.getPrivate(), "test-key");
        var jwks = JWKSet.parse(new JwkSetController(source).getJwks());
        var published = jwks.getKeys().get(0).toRSAKey();

        var now = Instant.now();
        var claims = JwtClaimsSet.builder()
                .issuer("auth-service").subject("buyer")
                .issuedAt(now).expiresAt(now.plusSeconds(60)).build();
        var token = config.jwtEncoder(source).encode(JwtEncoderParameters.from(claims));

        assertEquals(published.getKeyID(), token.getHeaders().get("kid"));
        assertFalse(published.isPrivate());
        assertEquals("buyer", config.jwtDecoder(published, "auth-service")
                .decode(token.getTokenValue()).getSubject());
        assertThrows(JwtValidationException.class, () -> config.jwtDecoder(published, "other-issuer")
                .decode(token.getTokenValue()));
    }
}
