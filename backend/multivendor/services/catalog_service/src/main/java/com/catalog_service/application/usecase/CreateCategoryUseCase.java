package com.catalog_service.application.usecase;

import com.catalog_service.application.command.CreateCategoryCommand;
import com.catalog_service.application.port.in.CategoryResult;
import com.catalog_service.application.port.out.CategoryRepository;
import com.catalog_service.domain.exception.CategorySlugAlreadyExistsException;
import com.catalog_service.domain.exception.ParentCategoryNotFoundException;
import com.catalog_service.domain.model.aggregate.Category;
import com.catalog_service.domain.model.vo.CategoryId;
import com.catalog_service.domain.model.vo.CategoryName;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class CreateCategoryUseCase {
    private final CategoryRepository repository;
    public CreateCategoryUseCase(CategoryRepository repository) { this.repository = repository; }

    @Transactional
    public CategoryResult execute(CreateCategoryCommand command) {
        if (repository.existsBySlug(command.slug())) throw new CategorySlugAlreadyExistsException(command.slug());
        CategoryId parentId = null;
        if (command.parentId() != null && !command.parentId().isBlank()) {
            parentId = CategoryId.of(UUID.fromString(command.parentId()));
            if (!repository.existsById(parentId)) throw new ParentCategoryNotFoundException(command.parentId());
        }
        return toResult(repository.save(Category.create(parentId, new CategoryName(command.name()), command.slug())));
    }

    static CategoryResult toResult(Category c) {
        return new CategoryResult(c.getCategoryId().value().toString(),
                c.getParentId() == null ? null : c.getParentId().value().toString(),
                c.getName().value(), c.getSlug(), c.getStatus().name(), c.getCreatedAt().toString(), c.getUpdatedAt().toString());
    }
}
