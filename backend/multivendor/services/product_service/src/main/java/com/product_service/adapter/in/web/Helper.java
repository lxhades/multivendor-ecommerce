package com.product_service.adapter.in.web;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public class Helper {

    public static UUID currentUserId(Jwt jwt) {
        String sub = jwt.getSubject();
        if (sub == null || sub.isBlank()) {
            throw new IllegalStateException("JWT không có subject");
        }
        return UUID.fromString(sub);
    }
}
