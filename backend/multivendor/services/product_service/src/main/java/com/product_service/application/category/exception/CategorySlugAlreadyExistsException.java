package com.product_service.application.category.exception;

public class CategorySlugAlreadyExistsException extends RuntimeException {

    public CategorySlugAlreadyExistsException(String slug) {
        super("Slug danh mục đã tồn tại: " + slug);
    }
}