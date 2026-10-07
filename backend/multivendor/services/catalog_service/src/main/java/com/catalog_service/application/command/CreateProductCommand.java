package com.catalog_service.application.command;

public record CreateProductCommand(
        String sellerId,
        String name,
        String description,
        String price
) {}
