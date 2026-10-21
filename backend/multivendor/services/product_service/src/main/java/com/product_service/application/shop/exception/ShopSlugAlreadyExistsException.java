package com.product_service.application.shop.exception;

public class ShopSlugAlreadyExistsException extends RuntimeException {
    public ShopSlugAlreadyExistsException(String slug) {
        super("Shop slug already exists: " + slug);
    }
}
