package com.product_service.adapter.in.web.category;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    public CategoryController(CreateCategoryUseCase create, GetCategoryUseCase get, ListCategoriesUseCase list,
                              UpdateCategoryUseCase update, DeleteCategoryUseCase delete,
                              ChangeCategoryStatusUseCase changeStatus) {
        this.create = create; this.get = get; this.list = list; this.update = update;
        this.delete = delete; this.changeStatus = changeStatus;
    }

    @PostMapping
    public ResponseEntity<CreateCategoryResult> create(@RequestBody CreateCategoryCommand command) {
        var result = create.create(command);
        return ResponseEntity.created(URI.create("/categories/" + result.id())).body(result);
    }

    @GetMapping("/{id}") public CreateCategoryResult get(@PathVariable UUID id) {
        return get.get(id);
    }

    @GetMapping public List<CreateCategoryResult> list() {
        return list.list();
    }

    @PutMapping("/{id}") public CreateCategoryResult update(
            @PathVariable UUID id,
            @RequestBody UpdateCategoryCommand command) {
        return update.update(id, command);
    }

    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) {
        delete.delete(id);
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/{id}/status") public CreateCategoryResult changeStatus(@PathVariable UUID id, @RequestParam CategoryStatus status) {
        return changeStatus.change(id, status);
    }
}
