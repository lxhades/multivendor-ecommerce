package com.product_service.domain.repository;

import com.product_service.domain.model.aggregate.Shop;
import com.product_service.domain.model.vo.SellerId;
import com.product_service.domain.model.vo.ShopId;
import com.product_service.domain.model.vo.Slug;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ShopRepository {
    Optional<Shop> findById(ShopId id);

    Page<Shop> findAll(Pageable pageable);

    Optional<Shop> findBySellerId(SellerId sellerId);

    Shop save(Shop aggregate);

    boolean existsBySlug(Slug slug, ShopId excludingId);

    boolean existsShopBySellerId(SellerId sellerId);
}
