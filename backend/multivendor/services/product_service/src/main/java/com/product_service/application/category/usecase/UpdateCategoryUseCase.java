package com.product_service.application.category.usecase;

import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import com.product_service.application.category.command.UpdateCategoryCommand;
import com.product_service.application.category.mapper.CategoryMapper;
import com.product_service.application.category.result.CreateCategoryResult;
import com.product_service.domain.model.aggregate.Category;
import com.product_service.domain.model.vo.CategoryId;
import com.product_service.domain.model.vo.Slug;
import com.product_service.domain.repository.CategoryRepository;
import com.product_service.domain.shared.Checks;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class UpdateCategoryUseCase {
    private final CategoryRepository repository;
    private final Clock clock;

    public UpdateCategoryUseCase(CategoryRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public CreateCategoryResult update(UUID id, UpdateCategoryCommand command) {
        Checks.required(command, "command");

        CategoryId categoryId = new CategoryId(id);
        Category category = repository.findById(categoryId)
                .orElseThrow(() -> new DomainException(ErrorCode.CATEGORY_NOT_FOUND));

        Slug slug = new Slug(command.slug());
        if (repository.existsBySlug(slug, categoryId)) throw new DomainException(ErrorCode.CATEGORY_SLUG_ALREADY_EXISTS);
        CategoryId parentId = command.parentId() == null ? null : new CategoryId(command.parentId());
        if (parentId != null) {
            Checks.rule(!parentId.equals(categoryId), "Category cannot be its own parent");
            repository.findById(parentId).
                    orElseThrow(() -> new DomainException(ErrorCode.CATEGORY_NOT_FOUND));
            Checks.rule(!repository.isDescendant(parentId, categoryId), "Category hierarchy cannot contain a cycle");
        }

        category.moveTo(parentId, clock.instant());
        category.update(command.name(),
                slug,
                command.description(),
                command.imageUrl(),
                command.displayOrder(),
                clock.instant());
        return CategoryMapper.toResult(repository.save(category));
    }
}
