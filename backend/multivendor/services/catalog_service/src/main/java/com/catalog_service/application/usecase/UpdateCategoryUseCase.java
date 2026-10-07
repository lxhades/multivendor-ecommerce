package com.catalog_service.application.usecase;

import com.catalog_service.application.command.UpdateCategoryCommand;
import com.catalog_service.application.port.in.CategoryResult;
import com.catalog_service.application.port.out.CategoryRepository;
import com.catalog_service.domain.exception.CategoryNotFoundException;
import com.catalog_service.domain.model.aggregate.Category;
import com.catalog_service.domain.model.vo.CategoryId;
import com.catalog_service.domain.model.vo.CategoryName;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class UpdateCategoryUseCase {
    private final CategoryRepository repository;
    public UpdateCategoryUseCase(CategoryRepository repository) { this.repository = repository; }

    @Transactional
    public CategoryResult execute(UpdateCategoryCommand command) {
        CategoryId id = CategoryId.of(UUID.fromString(command.categoryId()));
        Category category = repository.findById(id).orElseThrow(() -> new CategoryNotFoundException(command.categoryId()));
        if (!category.getSlug().equals(command.slug()) && repository.existsBySlug(command.slug())) throw new IllegalArgumentException("Category slug already exists");
        category.update(new CategoryName(command.name()), command.slug());
        return CreateCategoryUseCase.toResult(repository.save(category));
    }
}
