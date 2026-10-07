package com.catalog_service.adapters.in.web.controller;

import com.catalog_service.adapters.in.web.dto.request.CreateCategoryRequest;
import com.catalog_service.adapters.in.web.dto.request.UpdateCategoryRequest;
import com.catalog_service.adapters.in.web.dto.response.CategoryResponse;
import com.catalog_service.adapters.in.web.mapper.CategoryWebMapper;
import com.catalog_service.application.command.CreateCategoryCommand;
import com.catalog_service.application.command.UpdateCategoryCommand;
import com.catalog_service.application.query.GetCategoryQuery;
import com.catalog_service.application.query.ListCategoriesQuery;
import com.catalog_service.application.usecase.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/catalog/categories")
public class CategoryController {
    private final CreateCategoryUseCase create;
    private final GetCategoryUseCase get;
    private final ListCategoriesUseCase list;
    private final UpdateCategoryUseCase update;
    private final DeleteCategoryUseCase delete;
    private final CategoryWebMapper mapper;

    public CategoryController(CreateCategoryUseCase create, GetCategoryUseCase get, ListCategoriesUseCase list, UpdateCategoryUseCase update, DeleteCategoryUseCase delete, CategoryWebMapper mapper){this.create=create;this.get=get;this.list=list;this.update=update;this.delete=delete;this.mapper=mapper;}

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CreateCategoryRequest r){return mapper.toResponse(create.execute(new CreateCategoryCommand(r.parentId(),r.name(),r.slug())));}
    @GetMapping("/{categoryId}") public CategoryResponse get(@PathVariable String categoryId){return mapper.toResponse(get.execute(new GetCategoryQuery(categoryId)));}
    @GetMapping public List<CategoryResponse> list(@RequestParam(required=false) String parentId){return list.execute(new ListCategoriesQuery(parentId)).stream().map(mapper::toResponse).toList();}
    @PutMapping("/{categoryId}") public CategoryResponse update(@PathVariable String categoryId,@Valid @RequestBody UpdateCategoryRequest r){return mapper.toResponse(update.execute(new UpdateCategoryCommand(categoryId,r.name(),r.slug())));}
    @DeleteMapping("/{categoryId}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable String categoryId){delete.execute(categoryId);}
}
