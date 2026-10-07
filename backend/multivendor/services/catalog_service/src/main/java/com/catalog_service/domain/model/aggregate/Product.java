package com.catalog_service.domain.model.aggregate;

import com.catalog_service.domain.model.vo.Money;
import com.catalog_service.domain.model.vo.ProductId;
import com.catalog_service.domain.model.vo.ProductName;
import com.catalog_service.domain.model.enumtype.ProductStatus;
import com.catalog_service.domain.model.vo.SellerId;

import java.time.Instant;
import java.util.Objects;

public class Product {
    private final ProductId productId;
    private final SellerId sellerId;
    private ProductName name;
    private String description;
    private Money price;
    private ProductStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private Product(
            ProductId productId,
            SellerId sellerId,
            ProductName name,
            String description,
            Money price,
            ProductStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.productId = Objects.requireNonNull(productId);
        this.sellerId = Objects.requireNonNull(sellerId);
        this.name = Objects.requireNonNull(name);
        this.description = description;
        this.price = Objects.requireNonNull(price);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Product create(
            SellerId sellerId,
            ProductName name,
            String description,
            Money price
    ) {
        Instant now = Instant.now();
        return new Product(
                ProductId.generate(),
                sellerId,
                name,
                description,
                price,
                ProductStatus.DRAFT,
                now,
                now
        );
    }

    public static Product reconstitute(
            ProductId productId,
            SellerId sellerId,
            ProductName name,
            String description,
            Money price,
            ProductStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Product(
                productId, sellerId, name, description, price,
                status, createdAt, updatedAt
        );
    }

    public void update(
            ProductName name,
            String description,
            Money price,
            ProductStatus status
    ) {
        this.name = Objects.requireNonNull(name);
        this.description = description;
        this.price = Objects.requireNonNull(price);
        this.status = Objects.requireNonNull(status);
        this.updatedAt = Instant.now();
    }

    public ProductId getProductId() { return productId; }
    public SellerId getSellerId() { return sellerId; }
    public ProductName getName() { return name; }
    public String getDescription() { return description; }
    public Money getPrice() { return price; }
    public ProductStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
