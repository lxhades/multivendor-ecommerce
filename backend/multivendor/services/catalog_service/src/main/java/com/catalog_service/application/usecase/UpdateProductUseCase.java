package com.catalog_service.application.usecase;

import com.catalog_service.application.command.UpdateProductCommand;
import com.catalog_service.application.port.in.ProductResult;
import com.catalog_service.application.port.out.ProductRepository;
import com.catalog_service.domain.exception.ProductNotFoundException;
import com.catalog_service.domain.model.aggregate.Product;
import com.catalog_service.domain.model.vo.Money;
import com.catalog_service.domain.model.vo.ProductId;
import com.catalog_service.domain.model.vo.ProductName;
import com.catalog_service.domain.model.vo.ProductStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class UpdateProductUseCase {
    private final ProductRepository productRepository;

    public UpdateProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductResult execute(UpdateProductCommand command) {
        ProductId productId = ProductId.of(UUID.fromString(command.productId()));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.update(
                new ProductName(command.name()),
                command.description(),
                Money.of(new BigDecimal(command.price())),
                ProductStatus.valueOf(command.status())
        );

        return GetProductUseCase.toResult(productRepository.save(product));
    }
}
