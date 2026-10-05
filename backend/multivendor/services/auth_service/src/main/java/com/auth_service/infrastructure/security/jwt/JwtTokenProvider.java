package com.auth_service.infrastructure.security.jwt;

import com.auth_service.application.port.out.TokenProvider;
import com.auth_service.domain.model.aggregate.User;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.List;

@Component
public class JwtTokenProvider implements TokenProvider {

    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final long accessTokenExpiration;

    public JwtTokenProvider(
            JwtEncoder jwtEncoder,
            @Value("${security.jwt.issuer:auth-service}") String issuer,
            @Value("${auth.access-token.expiration:900}") long accessTokenExpiration
    ) {
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.accessTokenExpiration = accessTokenExpiration;
    }

    @Override
    public String generateAccessToken(User user) {

        Instant now = Instant.now();

        List<String> roles = user.getRoles()
                .stream()
                .map(Enum::name)
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(user.getUserId().toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(accessTokenExpiration))
                .claim("email", user.getEmail().getValue())
                .claim("roles", roles)
                .build();

        return jwtEncoder
                .encode(JwtEncoderParameters.from(claims))
                .getTokenValue();
    }
}