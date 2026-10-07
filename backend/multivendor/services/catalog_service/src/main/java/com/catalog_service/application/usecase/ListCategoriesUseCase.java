package com.catalog_service.application.usecase;

import com.catalog_service.application.port.in.CategoryResult;
import com.catalog_service.application.port.out.CategoryRepository;
import com.catalog_service.application.query.ListCategoriesQuery;
import com.catalog_service.domain.model.vo.CategoryId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class ListCategoriesUseCase {
    private final CategoryRepository repository;
    public ListCategoriesUseCase(CategoryRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public List<CategoryResult> execute(ListCategoriesQuery query) {
        if (query.parentId() == null || query.parentId().isBlank()) return repository.findAll().stream().map(CreateCategoryUseCase::toResult).toList();
        CategoryId parentId = CategoryId.of(UUID.fromString(query.parentId()));
        return repository.findByParentId(parentId).stream().map(CreateCategoryUseCase::toResult).toList();
    }
}
