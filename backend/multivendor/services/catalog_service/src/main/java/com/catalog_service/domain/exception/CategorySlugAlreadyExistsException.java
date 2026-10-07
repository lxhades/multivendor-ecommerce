package com.catalog_service.domain.exception;

public class CategorySlugAlreadyExistsException extends RuntimeException {
    public CategorySlugAlreadyExistsException(String message) {
        super("CategorySlugAlreadyExistsException "+ message);
    }
}
