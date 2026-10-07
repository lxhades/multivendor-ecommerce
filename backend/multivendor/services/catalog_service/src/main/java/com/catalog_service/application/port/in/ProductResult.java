package com.catalog_service.application.port.in;

public record ProductResult(
        String productId,
        String sellerId,
        String name,
        String description,
        String price,
        String status,
        String createdAt,
        String updatedAt
) {}
