package com.product_service.application.shop.result;

import com.product_service.domain.model.enumtype.ShopStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PublicShopResult(
        UUID id,
        UUID sellerId,
        String name,
        String slug,
        String logoUrl,
        String bannerUrl,
        String description,
        String address,
        ShopStatus status,
        BigDecimal ratingAvg,
        long ratingCount,
        long productCount,
        Instant createdAt,
        Instant updatedAt
) {
    public static PublicShopResult from(ShopResult shop) {
        return new PublicShopResult(
                shop.id(), shop.sellerId(), shop.name(), shop.slug(),
                shop.logoUrl(), shop.bannerUrl(), shop.description(), shop.address(),
                shop.status(), shop.ratingAvg(), shop.ratingCount(), shop.productCount(),
                shop.createdAt(), shop.updatedAt()
        );
    }
}
