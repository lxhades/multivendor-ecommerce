package com.inventory_service.adapter.out.persistence;

import com.inventory_service.adapter.out.persistence.entity.InventoryJpaEntity;
import com.inventory_service.adapter.out.persistence.mapper.InventoryPersistenceMapper;
import com.inventory_service.adapter.out.persistence.repository.InventoryJpaRepository;

import com.inventory_service.domain.model.aggregate.Inventory;
import com.inventory_service.domain.repository.InventoryRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional; // Nên thêm Transactional

import java.util.List;
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
        Optional<InventoryJpaEntity> existing = jpaRepository.findById(inventory.getId());

        if (existing.isPresent()) {
            InventoryJpaEntity jpaEntity = existing.get();
            jpaEntity.updateStock(
                    inventory.getOnHandQuantity(),
                    inventory.getReservedQuantity(),
                    inventory.getUpdatedAt()
            );
            return mapper.toDomain(jpaRepository.save(jpaEntity));
        }

        InventoryJpaEntity newEntity = mapper.toJpaEntity(inventory);
        return mapper.toDomain(jpaRepository.save(newEntity));
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