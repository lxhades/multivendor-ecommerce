package com.catalog_service.domain.exception;

import com.catalog_service.domain.model.vo.ProductId;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(ProductId productId) {
        super("Product not found: " + productId);
    }
}
