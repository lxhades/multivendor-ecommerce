package com.catalog_service.application.port.out;

import com.catalog_service.domain.model.aggregate.Category;
import com.catalog_service.domain.model.vo.CategoryId;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findById(CategoryId id);
    List<Category> findAll();
    List<Category> findByParentId(CategoryId parentId);
    boolean existsById(CategoryId id);
    boolean existsBySlug(String slug);
    void deleteById(CategoryId id);
}
