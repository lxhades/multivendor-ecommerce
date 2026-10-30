package com.product_service.application.category.usecase;

import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import com.product_service.application.category.mapper.CategoryMapper;
import com.product_service.application.category.result.CreateCategoryResult;
import com.product_service.domain.model.vo.CategoryId;
import com.product_service.domain.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GetCategoriesByParentIdUseCase {


    private final CategoryRepository repository;

    public GetCategoriesByParentIdUseCase(CategoryRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<CreateCategoryResult> get(UUID id) {
        CategoryId parentId = new CategoryId(id);
        if (!repository.existsById(parentId)) {
            throw new DomainException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        return repository.findByParentId(parentId).stream()
                .map(CategoryMapper::toResult)
                .toList();
    }
}
