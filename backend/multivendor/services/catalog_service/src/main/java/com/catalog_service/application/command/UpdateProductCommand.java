package com.catalog_service.application.command;

public record UpdateProductCommand(
        String productId,
        String name,
        String description,
        String price,
        String status
) {}
