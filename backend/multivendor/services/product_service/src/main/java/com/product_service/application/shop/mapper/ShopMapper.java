package com.product_service.application.shop.mapper;

import com.product_service.application.shop.result.ShopResult;
import com.product_service.domain.model.aggregate.Shop;

public final class ShopMapper {

    private ShopMapper() {
    }

    public static ShopResult toResult(Shop s) {
        return new ShopResult(
                s.getId().value(),
                s.getSellerId().value(),
                s.getName(),
                s.getSlug().value(),
                s.getLogoUrl(),
                s.getBannerUrl(),
                s.getDescription(),
                s.getAddress(),
                s.getBankDetails(),
                s.getStatus(),
                s.getRating().average(),
                s.getRating().count(),
                s.getProductCount(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }
}