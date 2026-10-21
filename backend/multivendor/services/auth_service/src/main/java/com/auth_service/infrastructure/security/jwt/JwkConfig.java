package com.auth_service.infrastructure.security.jwt;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.converter.RsaKeyConverters;

import java.io.InputStream;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class JwkConfig {

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        try {

            ClassPathResource privateResource = new ClassPathResource("certs/private_key.pem");
            RSAPrivateKey privateKey;
            try (InputStream is = privateResource.getInputStream()) {
                privateKey = RsaKeyConverters.pkcs8().convert(is);
            }


            ClassPathResource publicResource = new ClassPathResource("certs/public_key.pem");
            RSAPublicKey publicKey;
            try (InputStream is = publicResource.getInputStream()) {
                publicKey = RsaKeyConverters.x509().convert(is);
            }


            RSAKey rsaKey = new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID("auth-service-key-1") // ID định danh cho key này
                    .build();


            JWKSet jwkSet = new JWKSet(rsaKey);
            return (jwkSelector, context) -> jwkSelector.select(jwkSet);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tải cặp khóa RSA từ file PEM", e);
        }
    }
}