
package com.inventory_service.domain.repository;

import com.inventory_service.domain.model.aggregate.Inventory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository {

    Inventory save(Inventory inventory);

    Optional<Inventory> findById(UUID inventoryId);

    Optional<Inventory> findByVariantId(UUID variantId);

    List<Inventory> findByShopId(UUID shopId);

    boolean existsByVariantId(UUID variantId);
    void deleteByVariantId(UUID variantId);
    void deleteByShopId(UUID shopId);
    void deleteByProductId(UUID productId);
}
