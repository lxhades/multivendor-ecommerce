package com.product_service.adapter.in.web;

import org.springframework.security.core.Authentication;

import java.util.UUID;

public class Helper {

    public static UUID currentUserId(Authentication actor) {
        String sub = actor.getName();
        if (sub == null || sub.isBlank()) {
            throw new IllegalStateException("JWT không có subject");
        }
        return UUID.fromString(sub);
    }
}
