package com.product_service.adapter.out.persistence.shop.mapper;

import com.product_service.adapter.out.persistence.shop.entity.ShopJpaEntity;
import com.product_service.domain.model.aggregate.Shop;
import com.product_service.domain.model.vo.*;

public final class ShopPersistenceMapper {

    private ShopPersistenceMapper() {
    }

    public static ShopJpaEntity toEntity(Shop s) {
        return new ShopJpaEntity(
                s.getId().value(),
                s.getSellerId().value(),
                s.getName(),
                s.getSlug().value(),
                s.getLogoUrl(),
                s.getBannerUrl(),
                s.getDescription(),
                s.getAddress(),
                s.getBankDetails(),
                s.getRating().average(),
                s.getRating().count(),
                s.getProductCount(),
                s.getStatus(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }

    public static Shop toDomain(ShopJpaEntity e) {
        return Shop.restore(
                new ShopId(e.getId()),
                new SellerId(e.getSellerId()),
                e.getName(),
                new Slug(e.getSlug()),
                e.getLogoUrl(),
                e.getBannerUrl(),
                e.getDescription(),
                e.getAddress(),
                e.getBankDetails(),
                e.getStatus(),
                new RatingSummary(e.getRatingAvg(), e.getRatingCount()),
                e.getProductCount(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}