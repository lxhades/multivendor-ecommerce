package com.catalog_service.adapters.in.web.dto.response;
import java.time.Instant;
public record CategoryResponse(String categoryId, String parentId, String name, String slug, String status, Instant createdAt, Instant updatedAt) {}
