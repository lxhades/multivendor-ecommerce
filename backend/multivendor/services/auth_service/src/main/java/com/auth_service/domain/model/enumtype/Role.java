package com.auth_service.domain.model.enumtype;

import java.util.List;

public enum Role {
    BUYER,
    SELLER,
    ADMIN;

    public List<String> effectiveRoles() {
        return switch (this) {
            case BUYER -> List.of(BUYER.name());
            case SELLER -> List.of(BUYER.name(), SELLER.name());
            case ADMIN -> List.of(BUYER.name(), SELLER.name(), ADMIN.name());
        };
    }
}