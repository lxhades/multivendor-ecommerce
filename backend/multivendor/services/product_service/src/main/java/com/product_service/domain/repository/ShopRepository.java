package com.product_service.domain.repository;
import java.util.List;
import java.util.Optional;
import com.product_service.domain.model.aggregate.Shop;
import com.product_service.domain.model.vo.*;
public interface ShopRepository {
    Optional<Shop> findById(ShopId id);
    List<Shop> findAll();
    List<Shop> findBySellerId(SellerId sellerId);
    Shop save(Shop aggregate);
    boolean existsBySlug(Slug slug, ShopId excludingId);
}
