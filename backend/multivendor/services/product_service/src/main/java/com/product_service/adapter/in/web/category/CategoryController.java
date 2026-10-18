package com.product_service.adapter.in.web.category;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.product_service.adapter.in.web.response.ApiResponse;
import com.product_service.application.category.command.*;
import com.product_service.application.category.result.CreateCategoryResult;
import com.product_service.application.category.usecase.*;
import com.product_service.domain.model.enumtype.CategoryStatus;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CreateCategoryUseCase create;
    private final GetCategoryUseCase get;
    private final ListCategoriesUseCase list;
    private final UpdateCategoryUseCase update;
    private final DeleteCategoryUseCase delete;
    private final ChangeCategoryStatusUseCase changeStatus;

    public CategoryController(
            CreateCategoryUseCase create,
            GetCategoryUseCase get,
            ListCategoriesUseCase list,
            UpdateCategoryUseCase update,
            DeleteCategoryUseCase delete,
            ChangeCategoryStatusUseCase changeStatus) {
        this.create = create;
        this.get = get;
        this.list = list;
        this.update = update;
        this.delete = delete;
        this.changeStatus = changeStatus;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateCategoryResult>> create(@RequestBody CreateCategoryCommand command) {
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
            @RequestBody UpdateCategoryCommand command) {
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

            @RequestBody ChangeStatusCategoryCommand request) {
        CreateCategoryResult result = changeStatus.change(request.id(), request.status());

        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công", result));
    }


}