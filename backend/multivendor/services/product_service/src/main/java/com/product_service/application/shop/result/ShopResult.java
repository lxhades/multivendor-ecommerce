package com.product_service.application.shop.result;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.product_service.domain.model.enumtype.ShopStatus;

public record ShopResult(
        UUID id,
        UUID sellerId,
        String name,
        String slug,
        String logoUrl,
        String bannerUrl,
        String description,
        String address,
        String bankDetails,
        ShopStatus status,
        BigDecimal ratingAvg,
        long ratingCount,
        long productCount,
        Instant createdAt,
        Instant updatedAt
) {}