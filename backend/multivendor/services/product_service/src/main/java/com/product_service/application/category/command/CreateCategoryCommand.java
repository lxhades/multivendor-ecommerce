package com.product_service.application.category.command;


import java.util.UUID;

public record CreateCategoryCommand(
        UUID parentId,
        String name,
        String slug,
        String description,
        String imageUrl,
        int displayOrder
) {
}