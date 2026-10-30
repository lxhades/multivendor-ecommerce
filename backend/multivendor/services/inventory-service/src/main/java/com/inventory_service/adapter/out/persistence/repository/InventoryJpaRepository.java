package com.inventory_service.adapter.out.persistence.repository;

import com.inventory_service.adapter.out.persistence.entity.InventoryJpaEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryJpaRepository
        extends JpaRepository<InventoryJpaEntity, UUID> {

    Optional<InventoryJpaEntity> findByVariantId(UUID variantId);

    List<InventoryJpaEntity> findByShopId(UUID shopId);

    boolean existsByVariantId(UUID variantId);
}