package com.product_service.application.category.result;

import java.time.Instant;
import java.util.UUID;

public record CreateCategoryResult(
        UUID id,
        UUID parentId,
        String name,
        String slug,
        String description,
        String imageUrl,
        String status,
        int displayOrder,
        Instant createdAt,
        Instant updatedAt
) {
}