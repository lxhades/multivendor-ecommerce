package com.catalog_service.adapters.out.persistence.mapper;

import com.catalog_service.adapters.out.persistence.entity.CategoryJpaEntity;
import com.catalog_service.adapters.out.persistence.entity.CategoryStatusJpa;
import com.catalog_service.domain.model.aggregate.Category;
import com.catalog_service.domain.model.vo.CategoryId;
import com.catalog_service.domain.model.vo.CategoryName;
import com.catalog_service.domain.model.enumtype.CategoryStatus;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public CategoryJpaEntity toEntity(Category c) {
        return new CategoryJpaEntity(c.getCategoryId().value(), c.getParentId()==null?null:c.getParentId().value(), c.getName().value(), c.getSlug(), CategoryStatusJpa.valueOf(c.getStatus().name()), c.getCreatedAt(), c.getUpdatedAt());
    }
    public Category toDomain(CategoryJpaEntity e) {
        return Category.reconstitute(CategoryId.of(e.getId()), e.getParentId()==null?null:CategoryId.of(e.getParentId()), new CategoryName(e.getName()), e.getSlug(), CategoryStatus.valueOf(e.getStatus().name()), e.getCreatedAt(), e.getUpdatedAt());
    }
}
