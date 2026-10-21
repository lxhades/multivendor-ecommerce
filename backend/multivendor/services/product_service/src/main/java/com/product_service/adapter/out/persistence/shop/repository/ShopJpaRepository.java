package com.product_service.adapter.out.persistence.shop.repository;

import com.product_service.adapter.out.persistence.shop.entity.ShopJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShopJpaRepository extends JpaRepository<ShopJpaEntity, UUID> {
    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    Page<ShopJpaEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Optional<ShopJpaEntity> findBySellerId(UUID sellerId);

    boolean existsBySellerId(UUID sellerId);
}
