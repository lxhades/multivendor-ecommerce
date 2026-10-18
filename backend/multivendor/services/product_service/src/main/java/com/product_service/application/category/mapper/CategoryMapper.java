package com.product_service.application.category.mapper;

import com.product_service.application.category.result.CreateCategoryResult;
import com.product_service.domain.model.aggregate.Category;

public class CategoryMapper {
    public static CreateCategoryResult toResult(Category category) {
        return new CreateCategoryResult(
                category.getId().value(),
                category.getParentId() == null ? null : category.getParentId().value(),
                category.getName(),
                category.getSlug().value(),
                category.getDescription(),
                category.getImageUrl(),
                category.getStatus().name(),
                category.getDisplayOrder(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
