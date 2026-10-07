package com.catalog_service.adapters.in.web.controller;

import com.catalog_service.adapters.in.web.dto.request.CreateProductRequest;
import com.catalog_service.adapters.in.web.dto.request.UpdateProductRequest;
import com.catalog_service.adapters.in.web.dto.response.ProductResponse;
import com.catalog_service.adapters.in.web.mapper.ProductWebMapper;
import com.catalog_service.application.port.in.ProductResult;
import com.catalog_service.application.query.GetProductQuery;
import com.catalog_service.application.query.ListProductsQuery;
import com.catalog_service.application.usecase.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/catalog/products")
public class ProductController {
    private final CreateProductUseCase create;
    private final GetProductUseCase get;
    private final ListProductsUseCase list;
    private final UpdateProductUseCase update;
    private final DeleteProductUseCase delete;
    private final ProductWebMapper mapper;
    public ProductController(CreateProductUseCase create,GetProductUseCase get,ListProductsUseCase list,UpdateProductUseCase update,DeleteProductUseCase delete,ProductWebMapper mapper){this.create=create;this.get=get;this.list=list;this.update=update;this.delete=delete;this.mapper=mapper;}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public ProductResult create(@Valid @RequestBody CreateProductRequest r){return create.execute(mapper.toCommand(r));}
    @GetMapping("/{productId}") public ProductResult get(@PathVariable String productId){return get.execute(new GetProductQuery(productId));}
    @GetMapping public List<ProductResult> list(){return list.execute(new ListProductsQuery());}
    @PutMapping("/{productId}") public ProductResult update(@PathVariable String productId,@Valid @RequestBody UpdateProductRequest r){return update.execute(mapper.toCommand(productId,r));}
    @DeleteMapping("/{productId}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable String productId){delete.execute(productId);}
}
