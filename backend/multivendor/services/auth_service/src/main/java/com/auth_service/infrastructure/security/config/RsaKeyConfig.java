    package com.auth_service.infrastructure.security.config;

    import com.nimbusds.jose.jwk.JWKSet;
    import com.nimbusds.jose.jwk.RSAKey;
    import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
    import com.nimbusds.jose.jwk.source.JWKSource;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.core.io.ClassPathResource;
    import org.springframework.core.io.Resource;
    import com.nimbusds.jose.proc.SecurityContext;
    import org.springframework.security.oauth2.jwt.JwtDecoder;
    import org.springframework.security.oauth2.jwt.JwtEncoder;
    import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
    import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.security.oauth2.jwt.JwtValidators;

    import java.io.IOException;
    import java.nio.charset.StandardCharsets;
    import java.security.KeyFactory;
    import java.security.interfaces.RSAPrivateKey;
    import java.security.interfaces.RSAPublicKey;
    import java.security.spec.PKCS8EncodedKeySpec;
    import java.security.spec.X509EncodedKeySpec;
    import java.util.Base64;

    @Configuration
    public class RsaKeyConfig {

        private final Resource privateKeyResource =
                new ClassPathResource("certs/private_key.pem");

        private final Resource publicKeyResource =
                new ClassPathResource("certs/public_key.pem");

        @Bean
        public RSAPrivateKey rsaPrivateKey() throws Exception {
            String key = readKey(privateKeyResource);

            byte[] keyBytes = Base64.getDecoder().decode(key);

            PKCS8EncodedKeySpec keySpec =
                    new PKCS8EncodedKeySpec(keyBytes);

            KeyFactory keyFactory =
                    KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        }

        @Bean
        public RSAPublicKey rsaPublicKey() throws Exception {
            String key = readKey(publicKeyResource);

            byte[] keyBytes = Base64.getDecoder().decode(key);

            X509EncodedKeySpec keySpec =
                    new X509EncodedKeySpec(keyBytes);

            KeyFactory keyFactory =
                    KeyFactory.getInstance("RSA");

            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        }

        @Bean
        public JwtEncoder jwtEncoder(RSAPublicKey publicKey,
                                     RSAPrivateKey privateKey) {

            RSAKey rsaKey = new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .build();

            JWKSource<SecurityContext> jwkSource =
                    new ImmutableJWKSet<>(new JWKSet(rsaKey));

            return new NimbusJwtEncoder(jwkSource);
        }

        @Bean
        public JwtDecoder jwtDecoder(
                RSAPublicKey publicKey,
                @Value("${security.jwt.issuer:auth-service}") String issuer
        ) {
            NimbusJwtDecoder decoder = NimbusJwtDecoder
                    .withPublicKey(publicKey)
                    .build();
            decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
            return decoder;
        }
        private String readKey(Resource resource) throws IOException {
            String content = new String(
                    resource.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            return content
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
        }
    }