package com.product_service.application.category.usecase;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.product_service.application.category.exception.CategoryNotFoundException;
import com.product_service.application.category.mapper.CategoryMapper;
import com.product_service.application.category.result.CreateCategoryResult;
import com.product_service.domain.model.enumtype.CategoryStatus;
import com.product_service.domain.model.vo.CategoryId;
import com.product_service.domain.repository.CategoryRepository;
@Service
public class ChangeCategoryStatusUseCase {
    private final CategoryRepository repository;
    private final Clock clock;

    public ChangeCategoryStatusUseCase(CategoryRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public CreateCategoryResult change(UUID id, CategoryStatus status) {
        var category = repository.findById(new CategoryId(id))
                .orElseThrow(() -> new CategoryNotFoundException(id));
        category.changeStatus(status, clock.instant());
        return CategoryMapper.toResult(repository.save(category));
    }
}
