package com.catalog_service.domain.exception;

public class ParentCategoryNotFoundException extends RuntimeException {
    public ParentCategoryNotFoundException(String message) {
        super("ParentCategoryNotFound "+ message);
    }
}
