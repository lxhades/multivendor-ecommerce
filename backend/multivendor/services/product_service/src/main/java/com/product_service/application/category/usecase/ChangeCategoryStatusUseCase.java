package com.product_service.application.category.usecase;

import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import com.product_service.application.category.mapper.CategoryMapper;
import com.product_service.application.category.result.CreateCategoryResult;
import com.product_service.domain.model.enumtype.CategoryStatus;
import com.product_service.domain.model.vo.CategoryId;
import com.product_service.domain.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class ChangeCategoryStatusUseCase {
    private final CategoryRepository repository;
    private final Clock clock;

    public ChangeCategoryStatusUseCase(CategoryRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public CreateCategoryResult change(UUID id, String status) {
        CategoryStatus categoryStatus = CategoryStatus.valueOf(status);
        var category = repository.findById(new CategoryId(id))
                .orElseThrow(() -> new DomainException(ErrorCode.CATEGORY_NOT_FOUND));

        category.changeStatus(categoryStatus, clock.instant());
        return CategoryMapper.toResult(repository.save(category));
    }
}
