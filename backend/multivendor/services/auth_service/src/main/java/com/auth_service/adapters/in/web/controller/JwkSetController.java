package com.auth_service.adapters.in.web.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class JwkSetController {
    private final RSAKey signingKey;

    public JwkSetController(RSAKey signingKey) {
        this.signingKey = signingKey;
    }

    @GetMapping("/oauth2/jwks")
    public Map<String, Object> getJwks() {
        return new JWKSet(signingKey.toPublicJWK()).toJSONObject();
    }
}
