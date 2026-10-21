package com.product_service.application.category.usecase;

import com.product_service.application.category.command.CreateCategoryCommand;
import com.product_service.application.category.exception.CategoryNotFoundException;
import com.product_service.application.category.exception.CategorySlugAlreadyExistsException;
import com.product_service.application.category.mapper.CategoryMapper;
import com.product_service.application.category.result.CreateCategoryResult;
import com.product_service.domain.model.aggregate.Category;
import com.product_service.domain.model.vo.CategoryId;
import com.product_service.domain.model.vo.Slug;
import com.product_service.domain.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

@Service
public class CreateCategoryUseCase {
    private final CategoryRepository categoryRepository;

    private final Clock clock;

    public CreateCategoryUseCase(
            CategoryRepository categoryRepository,
            Clock clock
    ) {
        this.categoryRepository = Objects.requireNonNull(categoryRepository);
        this.clock = Objects.requireNonNull(clock);
    }

    @Transactional
    public CreateCategoryResult create(CreateCategoryCommand command) {
        Objects.requireNonNull(command, "command");

        Instant now = clock.instant();

        CategoryId parentId = command.parentId() == null ? null : new CategoryId(command.parentId());

        Slug slug = new Slug(command.slug());


        Category category = new Category(
                CategoryId.random(),
                parentId,
                command.name(),
                slug,
                now
        );


        category.update(
                command.name(),
                slug,
                command.description(),
                command.imageUrl(),
                command.displayOrder(),
                now
        );
        if (categoryRepository.existsBySlug(slug, null)) {
            throw new CategorySlugAlreadyExistsException(slug.value());
        }

        if (parentId != null) {
            categoryRepository.findById(parentId)
                    .orElseThrow(() ->
                            new CategoryNotFoundException(command.parentId())
                    );
        }

        Category savedCategory = categoryRepository.save(category);
        return CategoryMapper.toResult(savedCategory);

    }

}
