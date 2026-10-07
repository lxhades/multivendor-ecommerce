package com.catalog_service.domain.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String message) {
        super("CategoryNotFoundException "+ message);
    }
}
