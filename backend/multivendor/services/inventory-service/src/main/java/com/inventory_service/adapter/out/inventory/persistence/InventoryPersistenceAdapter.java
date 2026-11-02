package com.inventory_service.adapter.out.inventory.persistence;

import com.inventory_service.adapter.out.inventory.persistence.entity.InventoryJpaEntity;
import com.inventory_service.adapter.out.inventory.persistence.mapper.InventoryPersistenceMapper;
import com.inventory_service.adapter.out.inventory.persistence.repository.InventoryJpaRepository;

import com.inventory_service.domain.model.aggregate.Inventory;
import com.inventory_service.domain.repository.InventoryRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional; // Nên thêm Transactional

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Repository
public class InventoryPersistenceAdapter implements InventoryRepository {

    private final InventoryJpaRepository jpaRepository;
    private final InventoryPersistenceMapper mapper;

    public InventoryPersistenceAdapter(
            InventoryJpaRepository jpaRepository,
            InventoryPersistenceMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }


    @Override
    @Transactional
    public void deleteByVariantId(UUID variantId) {
        jpaRepository.deleteByVariantId(variantId);
    }

    @Override
    @Transactional
    public void deleteByShopId(UUID shopId) {
        jpaRepository.deleteByShopId(shopId);
    }

    @Override
    @Transactional
    public void deleteByProductId(UUID productId) {
        jpaRepository.deleteByProductId(productId);
    }


    @Override
    public Inventory save(Inventory inventory) {
        Objects.requireNonNull(inventory, "inventory is required");

        if (inventory.getVersion() == null) {
            InventoryJpaEntity newEntity = mapper.toJpaEntity(inventory);
            InventoryJpaEntity saved = jpaRepository.saveAndFlush(newEntity);
            return mapper.toDomain(saved);
        }

        InventoryJpaEntity entity =
                jpaRepository.findById(inventory.getId()).orElseThrow(() ->
                        new ObjectOptimisticLockingFailureException(
                                InventoryJpaEntity.class,
                                inventory.getId()
                ));

        if (!Objects.equals(entity.getVersion(), inventory.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(
                    InventoryJpaEntity.class, inventory.getId()
            );
        }

        entity.updateStock(
                inventory.getOnHandQuantity(),
                inventory.getReservedQuantity(),
                inventory.getUpdatedAt()
        );

        InventoryJpaEntity saved = jpaRepository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Inventory> findById(UUID inventoryId) {
        return jpaRepository.findById(inventoryId).map(mapper::toDomain);
    }

    @Override
    public Optional<Inventory> findByVariantId(UUID variantId) {
        return jpaRepository.findByVariantId(variantId).map(mapper::toDomain);
    }

    @Override
    public List<Inventory> findByShopId(UUID shopId) {
        return jpaRepository.findByShopId(shopId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByVariantId(UUID variantId) {
        return jpaRepository.existsByVariantId(variantId);
    }
}