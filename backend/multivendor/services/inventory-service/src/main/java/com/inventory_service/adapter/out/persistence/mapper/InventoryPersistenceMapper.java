package com.inventory_service.adapter.out.persistence.mapper;

import com.inventory_service.adapter.out.persistence.entity.InventoryJpaEntity;
import com.inventory_service.domain.model.aggregate.Inventory;
import org.springframework.stereotype.Component;

@Component
public class InventoryPersistenceMapper {

    public Inventory toDomain(InventoryJpaEntity jpaEntity) {

        return Inventory.restore(
                jpaEntity.getId(),
                jpaEntity.getProductId(),
                jpaEntity.getVariantId(),
                jpaEntity.getShopId(),
                jpaEntity.getOnHandQuantity(),
                jpaEntity.getReservedQuantity(),
                jpaEntity.getCreatedAt(),
                jpaEntity.getUpdatedAt()
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