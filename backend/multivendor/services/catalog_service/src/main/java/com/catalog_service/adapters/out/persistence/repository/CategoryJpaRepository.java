package com.catalog_service.adapters.out.persistence.repository;

import com.catalog_service.adapters.out.persistence.entity.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, UUID> {
    boolean existsBySlug(String slug);
    List<CategoryJpaEntity> findByParentId(UUID parentId);
}
