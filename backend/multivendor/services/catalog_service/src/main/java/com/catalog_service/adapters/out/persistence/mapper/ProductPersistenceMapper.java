package com.catalog_service.adapters.out.persistence.mapper;

import com.catalog_service.adapters.out.persistence.entity.ProductJpaEntity;
import com.catalog_service.domain.model.aggregate.Product;
import com.catalog_service.domain.model.vo.Money;
import com.catalog_service.domain.model.vo.ProductId;
import com.catalog_service.domain.model.vo.ProductName;
import com.catalog_service.domain.model.enumtype.ProductStatus;
import com.catalog_service.domain.model.vo.SellerId;
import org.springframework.stereotype.Component;

@Component
public class ProductPersistenceMapper {
    public ProductJpaEntity toEntity(Product p) {
        return new ProductJpaEntity(
                p.getProductId().value(), p.getSellerId().value(), p.getName().value(), p.getDescription(),
                p.getPrice().amount(), ProductJpaEntity.Status.valueOf(p.getStatus().name()),
                p.getCreatedAt(), p.getUpdatedAt()
        );
    }

    public Product toDomain(ProductJpaEntity e) {
        return Product.reconstitute(
                ProductId.of(e.getId()), SellerId.of(e.getSellerId()), new ProductName(e.getName()),
                e.getDescription(), Money.of(e.getPrice()), ProductStatus.valueOf(e.getStatus().name()),
                e.getCreatedAt(), e.getUpdatedAt()
        );
    }
}
