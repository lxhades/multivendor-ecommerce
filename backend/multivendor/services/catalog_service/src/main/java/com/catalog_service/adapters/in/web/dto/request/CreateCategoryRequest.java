package com.catalog_service.adapters.in.web.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record CreateCategoryRequest(String parentId, @NotBlank @Size(max=255) String name, @NotBlank @Size(max=255) String slug) {}
