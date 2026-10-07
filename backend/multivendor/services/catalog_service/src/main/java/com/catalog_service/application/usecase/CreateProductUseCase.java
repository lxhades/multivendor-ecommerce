package com.catalog_service.application.usecase;

import com.catalog_service.application.command.CreateProductCommand;
import com.catalog_service.application.port.in.ProductResult;
import com.catalog_service.application.port.out.ProductRepository;
import com.catalog_service.domain.model.aggregate.Product;
import com.catalog_service.domain.model.vo.Money;
import com.catalog_service.domain.model.vo.ProductName;
import com.catalog_service.domain.model.vo.SellerId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class CreateProductUseCase {
    private final ProductRepository productRepository;

    public CreateProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductResult execute(CreateProductCommand command) {
        Product product = Product.create(
                SellerId.of(UUID.fromString(command.sellerId())),
                new ProductName(command.name()),
                command.description(),
                Money.of(new BigDecimal(command.price()))
        );

        return toResult(productRepository.save(product));
    }

    private ProductResult toResult(Product p) {
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
