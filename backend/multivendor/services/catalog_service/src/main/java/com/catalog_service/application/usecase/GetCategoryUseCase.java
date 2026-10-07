package com.catalog_service.application.usecase;

import com.catalog_service.application.port.in.CategoryResult;
import com.catalog_service.application.port.out.CategoryRepository;
import com.catalog_service.application.query.GetCategoryQuery;
import com.catalog_service.domain.exception.CategoryNotFoundException;
import com.catalog_service.domain.model.vo.CategoryId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class GetCategoryUseCase {
    private final CategoryRepository repository;
    public GetCategoryUseCase(CategoryRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public CategoryResult execute(GetCategoryQuery query) {
        CategoryId id = CategoryId.of(UUID.fromString(query.categoryId()));
        return repository.findById(id).map(category -> CreateCategoryUseCase.toResult(category))
                .orElseThrow(() -> new CategoryNotFoundException(query.categoryId()));
    }
}
