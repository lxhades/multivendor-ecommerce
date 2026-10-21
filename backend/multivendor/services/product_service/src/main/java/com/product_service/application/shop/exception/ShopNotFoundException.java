package com.product_service.application.shop.exception;

import java.util.UUID;

public class ShopNotFoundException extends RuntimeException {
    public ShopNotFoundException(UUID id) {
        super("Shop not found: " + id);
    }
}
