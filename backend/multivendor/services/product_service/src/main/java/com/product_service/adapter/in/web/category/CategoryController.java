package com.product_service.adapter.in.web.category;

import com.product_service.adapter.in.web.dto.request.ChangeStatusCategoryRequest;
import com.product_service.adapter.in.web.dto.request.CreateCategoryRequest;
import com.product_service.adapter.in.web.dto.request.UpdateCategoryRequest;
import com.product_service.adapter.in.web.dto.response.ApiResponse;
import com.product_service.application.category.command.CreateCategoryCommand;
import com.product_service.application.category.command.UpdateCategoryCommand;
import com.product_service.application.category.result.CreateCategoryResult;
import com.product_service.application.category.usecase.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CreateCategoryUseCase create;
    private final GetCategoryUseCase get;
    private final ListCategoriesUseCase list;
    private final UpdateCategoryUseCase update;
    private final DeleteCategoryUseCase delete;
    private final ChangeCategoryStatusUseCase changeStatus;
    private final GetCategoroiesByParentIdUseCase getCategoroiesByParentIdUseCase;

    public CategoryController(
            CreateCategoryUseCase create,
            GetCategoryUseCase get,
            ListCategoriesUseCase list,
            UpdateCategoryUseCase update,
            DeleteCategoryUseCase delete,
            ChangeCategoryStatusUseCase changeStatus,
            GetCategoroiesByParentIdUseCase getCategoroiesByParentIdUseCase) {
        this.create = create;
        this.get = get;
        this.list = list;
        this.update = update;
        this.delete = delete;
        this.changeStatus = changeStatus;
        this.getCategoroiesByParentIdUseCase = getCategoroiesByParentIdUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateCategoryResult>> create(
            @Valid @RequestBody CreateCategoryRequest request) {

        CreateCategoryCommand command = new CreateCategoryCommand(
                request.parentId(),
                request.name(),
                request.slug(),
                request.description(),
                request.imageUrl(),
                request.displayOrder()
        );

        CreateCategoryResult result = create.create(command);

        return ResponseEntity
                .created(URI.create("/categories/" + result.id()))
                .body(ApiResponse.success("Tạo danh mục thành công", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CreateCategoryResult>> get(@PathVariable UUID id) {
        CreateCategoryResult result = get.get(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CreateCategoryResult>>> list() {
        List<CreateCategoryResult> results = list.list();
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CreateCategoryResult>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request) {

        UpdateCategoryCommand command = new UpdateCategoryCommand(
                request.parentId(),
                request.name(),
                request.slug(),
                request.description(),
                request.imageUrl(),
                request.displayOrder()
        );

        CreateCategoryResult result = update.update(id, command);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật danh mục thành công", result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        delete.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa danh mục thành công", null));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<CreateCategoryResult>> changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeStatusCategoryRequest request) {

        CreateCategoryResult result = changeStatus.change(id, request.status());

        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công", result));
    }

    @GetMapping("/{parentId}/children")
    public ResponseEntity<ApiResponse<List<CreateCategoryResult>>> getCategoriesByParentId(@PathVariable UUID parentId) {
        return ResponseEntity.ok(ApiResponse.success(getCategoroiesByParentIdUseCase.get(parentId)));
    }
}