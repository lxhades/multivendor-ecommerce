package com.product_service.adapter.out.persistence.category.mapper;

import com.product_service.adapter.out.persistence.category.entity.CategoryJpaEntity;
import com.product_service.domain.model.aggregate.Category;
import com.product_service.domain.model.vo.CategoryId;
import com.product_service.domain.model.vo.Slug;

public final class CategoryPersistenceMapper {

    private CategoryPersistenceMapper() {
    }

    public static CategoryJpaEntity toEntity(Category category) {
        return new CategoryJpaEntity(
                category.getId().value(),
                category.getParentId() == null
                        ? null
                        : category.getParentId().value(),
                category.getName(),
                category.getSlug().value(),
                category.getDescription(),
                category.getImageUrl(),
                category.getStatus(),
                category.getDisplayOrder(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    public static Category toDomain(CategoryJpaEntity entity) {
        return Category.restore(
                new CategoryId(entity.getId()),
                entity.getParentId() == null
                        ? null
                        : new CategoryId(entity.getParentId()),
                entity.getName(),
                new Slug(entity.getSlug()),
                entity.getDescription(),
                entity.getImageUrl(),
                entity.getStatus(),
                entity.getDisplayOrder(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}