package com.product_service.application.category.usecase;

import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import com.product_service.application.category.mapper.CategoryMapper;
import com.product_service.application.category.result.CreateCategoryResult;
import com.product_service.domain.model.aggregate.Category;
import com.product_service.domain.model.vo.CategoryId;
import com.product_service.domain.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetCategoryUseCase {
    private final CategoryRepository repository;

    public GetCategoryUseCase(CategoryRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public CreateCategoryResult get(UUID id) {
        Category category = repository.findById(new CategoryId(id))
                .orElseThrow(() -> new DomainException(ErrorCode.CATEGORY_NOT_FOUND));
        String parentName = category.getParentId() == null ? null
                : repository.findById(category.getParentId()).map(Category::getName).orElse(null);
        return CategoryMapper.toResult(category, parentName);
    }
}
