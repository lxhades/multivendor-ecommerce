package com.product_service.application.category.usecase;

import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import com.product_service.application.category.mapper.CategoryMapper;
import com.product_service.application.category.result.CreateCategoryResult;
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
        return repository.findById(new CategoryId(id))
                .map(CategoryMapper::toResult)
                .orElseThrow(() -> new DomainException(ErrorCode.CATEGORY_NOT_FOUND));
    }
}
