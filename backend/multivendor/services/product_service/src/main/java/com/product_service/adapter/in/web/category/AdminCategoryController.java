package com.product_service.adapter.in.web.category;

import com.product_service.adapter.in.web.dto.request.ChangeStatusCategoryRequest;
import com.product_service.adapter.in.web.dto.request.CreateCategoryRequest;
import com.product_service.adapter.in.web.dto.request.UpdateCategoryRequest;
import com.product_service.adapter.in.web.dto.response.ApiResponse;
import com.product_service.application.category.command.CreateCategoryCommand;
import com.product_service.application.category.command.UpdateCategoryCommand;
import com.product_service.application.category.result.CreateCategoryResult;
import com.product_service.application.category.usecase.ChangeCategoryStatusUseCase;
import com.product_service.application.category.usecase.CreateCategoryUseCase;
import com.product_service.application.category.usecase.DeleteCategoryUseCase;
import com.product_service.application.category.usecase.UpdateCategoryUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CreateCategoryUseCase createUseCase;
    private final UpdateCategoryUseCase updateUseCase;
    private final DeleteCategoryUseCase deleteUseCase;
    private final ChangeCategoryStatusUseCase changeStatusUseCase;

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

        CreateCategoryResult result = createUseCase.create(command);

        return ResponseEntity
                .created(URI.create("/categories/" + result.id()))
                .body(ApiResponse.success("Tạo danh mục thành công", result));
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

        CreateCategoryResult result = updateUseCase.update(id, command);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật danh mục thành công", result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        deleteUseCase.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa danh mục thành công", null));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<CreateCategoryResult>> changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeStatusCategoryRequest request) {

        CreateCategoryResult result = changeStatusUseCase.change(id, request.status());

        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công", result));
    }
}