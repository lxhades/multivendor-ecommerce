package com.catalog_service.application.usecase;

import com.catalog_service.application.port.out.CategoryRepository;
import com.catalog_service.domain.model.vo.CategoryId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class DeleteCategoryUseCase {
    private final CategoryRepository repository;
    public DeleteCategoryUseCase(CategoryRepository repository) { this.repository = repository; }
    @Transactional
    public void execute(String categoryId) {
        CategoryId id = CategoryId.of(UUID.fromString(categoryId));
        if (!repository.existsById(id)) throw new IllegalArgumentException("Category not found");
        repository.deleteById(id);
    }
}
