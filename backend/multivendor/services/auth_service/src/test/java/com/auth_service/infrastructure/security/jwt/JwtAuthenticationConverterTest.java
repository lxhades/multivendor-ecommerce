package com.auth_service.infrastructure.security.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationConverterTest {

    private final JwtAuthenticationConverter converter =
            new JwtAuthenticationConverter();

    @Test
    void convertsRolesToSpringSecurityAuthorities() {
        Jwt jwt = jwt(List.of("ADMIN", "SELLER"), "user-123");

        Authentication authentication = converter.convert(jwt);

        assertInstanceOf(JwtAuthenticationToken.class, authentication);
        assertEquals("user-123", authentication.getName());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SELLER")));
    }

    @Test
    void missingRolesProducesNoAuthorities() {
        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(60),
                Map.of("alg", "RS256"),
                Map.of("sub", "user-123")
        );

        Authentication authentication = converter.convert(jwt);

        assertTrue(authentication.getAuthorities().isEmpty());
    }

    private Jwt jwt(List<String> roles, String subject) {
        return new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(60),
                Map.of("alg", "RS256"),
                Map.of("sub", subject, "roles", roles)
        );
    }
}
