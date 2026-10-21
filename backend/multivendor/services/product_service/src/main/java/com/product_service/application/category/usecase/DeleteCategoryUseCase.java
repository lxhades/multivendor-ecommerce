package com.product_service.application.category.usecase;

import com.product_service.application.category.exception.CategoryNotFoundException;
import com.product_service.domain.model.enumtype.CategoryStatus;
import com.product_service.domain.model.vo.CategoryId;
import com.product_service.domain.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class DeleteCategoryUseCase {
    private final CategoryRepository repository;
    private final Clock clock;

    public DeleteCategoryUseCase(CategoryRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public void delete(UUID id) {
        var category = repository.findById(new CategoryId(id))
                .orElseThrow(() -> new CategoryNotFoundException(id));

        if (category.getStatus() != CategoryStatus.INACTIVE) {
            category.changeStatus(CategoryStatus.INACTIVE, clock.instant());
            repository.save(category);
        }
    }
}
