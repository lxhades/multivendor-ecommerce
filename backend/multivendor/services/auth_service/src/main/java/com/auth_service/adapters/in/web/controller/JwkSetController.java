package com.auth_service.adapters.in.web.controller;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class JwkSetController {

    private final JWKSource<SecurityContext> jwkSource;

    public JwkSetController(JWKSource<SecurityContext> jwkSource) {
        this.jwkSource = jwkSource;
    }

    @GetMapping("/oauth2/jwks")
    public Map<String, Object> getJwks() {
        try {
            // Lấy toàn bộ key có trong JWKSource
            JWKSelector selector = new JWKSelector(new JWKMatcher.Builder().build());

            // jwkSource.get(...) ném ra KeySourceException (checked exception)
            JWKSet jwkSet = new JWKSet(jwkSource.get(selector, null));

            // Trả về định dạng JSON chuẩn của JWK Set
            return jwkSet.toJSONObject();
        } catch (KeySourceException e) {
            // Xử lý exception: có thể log lại và ném ra lỗi Runtime để Spring trả về 500
            throw new RuntimeException("Không thể lấy JWK Set từ JWKSource", e);
        }
    }
}