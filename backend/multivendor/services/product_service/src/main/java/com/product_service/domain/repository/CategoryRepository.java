package com.product_service.domain.repository;

import java.util.Optional;
import com.product_service.domain.model.aggregate.Category;
import com.product_service.domain.model.vo.*;
public interface CategoryRepository {
    Optional<Category> findById(CategoryId id);
    Category save(Category aggregate);
    boolean existsBySlug(Slug slug, CategoryId excludingId);
    boolean isDescendant(CategoryId candidate, CategoryId ancestor);
}
