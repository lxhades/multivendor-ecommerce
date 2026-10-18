package com.product_service.application.category.usecase;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.product_service.application.category.exception.CategoryNotFoundException;
import com.product_service.application.category.mapper.CategoryMapper;
import com.product_service.application.category.result.CreateCategoryResult;
import com.product_service.domain.model.vo.CategoryId;
import com.product_service.domain.repository.CategoryRepository;
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
            .orElseThrow(() -> new CategoryNotFoundException(id));
    }
}
