package com.catalog_service.application.usecase;

import com.catalog_service.application.port.in.ProductResult;
import com.catalog_service.application.port.out.ProductRepository;
import com.catalog_service.application.query.ListProductsQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListProductsUseCase {
    private final ProductRepository productRepository;

    public ListProductsUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResult> execute(ListProductsQuery query) {
        return productRepository.findAll()
                .stream()
                .map(GetProductUseCase::toResult)
                .toList();
    }
}
