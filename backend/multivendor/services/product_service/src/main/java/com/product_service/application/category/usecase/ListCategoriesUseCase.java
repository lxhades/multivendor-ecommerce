package com.product_service.application.category.usecase;

import com.product_service.application.category.mapper.CategoryMapper;
import com.product_service.application.category.result.CreateCategoryResult;
import com.product_service.domain.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import com.product_service.domain.model.aggregate.Category;

@Service
public class ListCategoriesUseCase {
    private final CategoryRepository repository;

    public ListCategoriesUseCase(CategoryRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<CreateCategoryResult> list() {
        var categories = repository.findAll();
        var names = categories.stream().collect(Collectors.toMap(Category::getId, Category::getName));
        return categories.stream()
                .map(category -> CategoryMapper.toResult(category,
                        category.getParentId() == null ? null : names.get(category.getParentId())))
                .toList();
    }
}
