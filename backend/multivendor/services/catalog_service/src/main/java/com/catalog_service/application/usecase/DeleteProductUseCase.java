package com.catalog_service.application.usecase;

import com.catalog_service.application.port.out.ProductRepository;
import com.catalog_service.domain.exception.ProductNotFoundException;
import com.catalog_service.domain.model.vo.ProductId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteProductUseCase {
    private final ProductRepository productRepository;

    public DeleteProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public void execute(String productId) {
        ProductId id = ProductId.of(UUID.fromString(productId));
        productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        productRepository.deleteById(id);
    }
}
