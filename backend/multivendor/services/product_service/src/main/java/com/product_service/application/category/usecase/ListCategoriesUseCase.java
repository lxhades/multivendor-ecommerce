package com.product_service.application.category.usecase;

import com.product_service.application.category.mapper.CategoryMapper;
import com.product_service.application.category.result.CreateCategoryResult;
import com.product_service.domain.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListCategoriesUseCase {
    private final CategoryRepository repository;

    public ListCategoriesUseCase(CategoryRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<CreateCategoryResult> list() {
        return repository.findAll().stream().map(CategoryMapper::toResult).toList();
    }
}
