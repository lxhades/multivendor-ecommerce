package com.catalog_service.application.usecase;

import com.catalog_service.application.port.in.ProductResult;
import com.catalog_service.application.port.out.ProductRepository;
import com.catalog_service.application.query.GetProductQuery;
import com.catalog_service.domain.exception.ProductNotFoundException;
import com.catalog_service.domain.model.aggregate.Product;
import com.catalog_service.domain.model.vo.ProductId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetProductUseCase {
    private final ProductRepository productRepository;

    public GetProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public ProductResult execute(GetProductQuery query) {
        Product product = productRepository.findById(
                ProductId.of(UUID.fromString(query.productId()))
        ).orElseThrow(() ->
                new ProductNotFoundException(ProductId.of(UUID.fromString(query.productId())))
        );

        return toResult(product);
    }

    static ProductResult toResult(Product p) {
        return new ProductResult(
                p.getProductId().value().toString(),
                p.getSellerId().value().toString(),
                p.getName().value(),
                p.getDescription(),
                p.getPrice().amount().toPlainString(),
                p.getStatus().name(),
                p.getCreatedAt().toString(),
                p.getUpdatedAt().toString()
        );
    }
}
