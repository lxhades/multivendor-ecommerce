package com.catalog_service.application.port.in;
public record CategoryResult(String categoryId, String parentId, String name, String slug, String status,
                             String createdAt, String updatedAt) {}
