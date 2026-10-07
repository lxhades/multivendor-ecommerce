package com.catalog_service.infrastructure.web.controller;

import com.catalog_service.application.port.in.ProductResult;
import com.catalog_service.application.query.GetProductQuery;
import com.catalog_service.application.query.ListProductsQuery;
import com.catalog_service.application.usecase.*;
import com.catalog_service.infrastructure.web.dto.CreateProductRequest;
import com.catalog_service.infrastructure.web.dto.UpdateProductRequest;
import com.catalog_service.infrastructure.web.mapper.ProductWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalog/products")
public class ProductController {
    private final CreateProductUseCase createProductUseCase;
    private final GetProductUseCase getProductUseCase;
    private final ListProductsUseCase listProductsUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;
    private final ProductWebMapper mapper = new ProductWebMapper();

    public ProductController(
            CreateProductUseCase createProductUseCase,
            GetProductUseCase getProductUseCase,
            ListProductsUseCase listProductsUseCase,
            UpdateProductUseCase updateProductUseCase,
            DeleteProductUseCase deleteProductUseCase
    ) {
        this.createProductUseCase = createProductUseCase;
        this.getProductUseCase = getProductUseCase;
        this.listProductsUseCase = listProductsUseCase;
        this.updateProductUseCase = updateProductUseCase;
        this.deleteProductUseCase = deleteProductUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResult create(@Valid @RequestBody CreateProductRequest request) {
        return createProductUseCase.execute(mapper.toCommand(request));
    }

    @GetMapping("/{productId}")
    public ProductResult get(@PathVariable String productId) {
        return getProductUseCase.execute(new GetProductQuery(productId));
    }

    @GetMapping
    public List<ProductResult> list() {
        return listProductsUseCase.execute(new ListProductsQuery());
    }

    @PutMapping("/{productId}")
    public ProductResult update(
            @PathVariable String productId,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        return updateProductUseCase.execute(mapper.toCommand(productId, request));
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String productId) {
        deleteProductUseCase.execute(productId);
    }
}
