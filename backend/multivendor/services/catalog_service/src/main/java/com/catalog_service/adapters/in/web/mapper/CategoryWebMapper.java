package com.catalog_service.adapters.in.web.mapper;
import com.catalog_service.application.port.in.CategoryResult;
import com.catalog_service.adapters.in.web.dto.response.CategoryResponse;
import org.springframework.stereotype.Component;
@Component
public class CategoryWebMapper {
    public CategoryResponse toResponse(CategoryResult r){return new CategoryResponse(r.categoryId(),r.parentId(),r.name(),r.slug(),r.status(),java.time.Instant.parse(r.createdAt()),java.time.Instant.parse(r.updatedAt()));}
}
