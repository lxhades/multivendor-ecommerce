package com.inventory_service.adapter.out.inventory.persistence.mapper;

import com.inventory_service.adapter.out.inventory.persistence.entity.InventoryJpaEntity;
import com.inventory_service.domain.model.aggregate.Inventory;
import org.springframework.stereotype.Component;

@Component
public class InventoryPersistenceMapper {

    public Inventory toDomain(InventoryJpaEntity entity) {
        return Inventory.restore(
                entity.getId(),
                entity.getProductId(),
                entity.getVariantId(),
                entity.getShopId(),
                entity.getOnHandQuantity(),
                entity.getReservedQuantity(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }

    public InventoryJpaEntity toJpaEntity(Inventory inventory) {

        return new InventoryJpaEntity(
                inventory.getId(),
                inventory.getProductId(),
                inventory.getVariantId(),
                inventory.getShopId(),
                inventory.getOnHandQuantity(),
                inventory.getReservedQuantity(),
                inventory.getCreatedAt(),
                inventory.getUpdatedAt()
        );
    }
}