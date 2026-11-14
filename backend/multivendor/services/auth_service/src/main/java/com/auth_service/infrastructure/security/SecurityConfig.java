package com.auth_service.infrastructure.security;

import com.auth_service.infrastructure.security.handler.JwtAccessDeniedHandler;
import com.auth_service.infrastructure.security.handler.JwtAuthenticationEntryPoint;
import com.auth_service.infrastructure.security.jwt.JwtAuthenticationConverter;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain authSecurity(
            HttpSecurity http,
            JwtAuthenticationConverter roles,
            JwtAuthenticationEntryPoint unauthorized,
            JwtAccessDeniedHandler forbidden
    ) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(routes -> routes
                        .requestMatchers("/actuator/health", "/actuator/health/liveness", "/actuator/health/readiness").permitAll()
//                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**",
//                                "/swagger-resources/**", "/webjars/**").permitAll()
//                        .requestMatchers(HttpMethod.GET, "/oauth2/jwks").permitAll()
//
//                        .requestMatchers("/auth/register", "/auth/login", "/auth/refresh",
//                                "/auth/logout", "/auth/forgotpassword").permitAll()

                        .requestMatchers(HttpMethod.POST, "/auth/me/register-seller").hasRole("BUYER")

                        .requestMatchers("/auth/me", "/auth/changepassword",
                                "/auth/users/*/addresses", "/auth/users/*/addresses/**").authenticated()

                        .requestMatchers("/api/users/admin", "/api/users/admin/**").hasRole("ADMIN")


                        .anyRequest().permitAll())
                .exceptionHandling(errors -> errors.authenticationEntryPoint(unauthorized)
                        .accessDeniedHandler(forbidden))
                .oauth2ResourceServer(server -> server.authenticationEntryPoint(unauthorized)
                        .accessDeniedHandler(forbidden)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(roles)))
                .build();
    }

    // Cùng một cặp khóa dùng để ký, xác minh và công bố JWKS.
    @Bean
    public RSAKey signingKey(
            @Value("${security.jwt.private-key-location}") Resource privateKeyResource,
            @Value("${security.jwt.public-key-location}") Resource publicKeyResource,
            @Value("${security.jwt.key-id}") String keyId
    ) throws IOException {
        try (var privatePem = privateKeyResource.getInputStream();
             var publicPem = publicKeyResource.getInputStream()) {
            return rsaKey(RsaKeyConverters.x509().convert(publicPem),
                    RsaKeyConverters.pkcs8().convert(privatePem), keyId);
        }
    }

    RSAKey rsaKey(RSAPublicKey publicKey, RSAPrivateKey privateKey, String keyId) {
        if (!publicKey.getModulus().equals(privateKey.getModulus())) {
            throw new IllegalStateException("Cặp khóa không hợp lệ");
        }

        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(keyId)
                .build();
    }

    @Bean
    public JwtEncoder jwtEncoder(RSAKey signingKey) {
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(signingKey)));
    }

    @Bean
    public JwtDecoder jwtDecoder(
            RSAKey signingKey,
            @Value("${security.jwt.issuer}") String issuer
    ) throws JOSEException {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(signingKey.toRSAPublicKey())
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        return decoder;
    }
}
