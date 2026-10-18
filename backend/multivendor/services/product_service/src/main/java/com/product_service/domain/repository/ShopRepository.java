package com.product_service.domain.repository;

import java.util.Optional;
import com.product_service.domain.model.aggregate.Shop;
import com.product_service.domain.model.vo.*;
public interface ShopRepository {
    Optional<Shop> findById(ShopId id);
    Shop save(Shop aggregate);
    boolean existsBySlug(Slug slug, ShopId excludingId);
}
