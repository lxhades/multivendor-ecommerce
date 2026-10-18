package com.product_service.application.category.exception;

import java.util.UUID;

public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(UUID categoryId) {
        super("Không tìm thấy danh mục: " + categoryId);
    }
}