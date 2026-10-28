package com.product_service.adapter.in.web.category;

import com.product_service.adapter.in.web.dto.response.ApiResponse;
import com.product_service.application.category.result.CreateCategoryResult;
import com.product_service.application.category.usecase.GetCategoriesByParentIdUseCase;
import com.product_service.application.category.usecase.GetCategoryUseCase;
import com.product_service.application.category.usecase.ListCategoriesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final GetCategoryUseCase getCategoryUseCase;
    private final ListCategoriesUseCase listCategoriesUseCase;
    private final GetCategoriesByParentIdUseCase getCategoriesByParentIdUseCase;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CreateCategoryResult>> get(@PathVariable UUID id) {
        CreateCategoryResult result = getCategoryUseCase.get(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CreateCategoryResult>>> list() {
        List<CreateCategoryResult> results = listCategoriesUseCase.list();
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/{parentId}/children")
    public ResponseEntity<ApiResponse<List<CreateCategoryResult>>> getCategoriesByParentId(
            @PathVariable UUID parentId) {
        return ResponseEntity.ok(ApiResponse.success(getCategoriesByParentIdUseCase.get(parentId)));
    }
}