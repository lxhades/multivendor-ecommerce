package com.inventory_service.adapter.out.stockreservation.persistence.repository;

import com.inventory_service.adapter.out.stockreservation.persistence.entity.StockReservationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockReservationJpaRepository
        extends JpaRepository<StockReservationJpaEntity, UUID> {

    Optional<StockReservationJpaEntity> findByOrderIdAndInventoryId(
            UUID orderId,
            UUID inventoryId
    );

    List<StockReservationJpaEntity> findByOrderId(UUID orderId);
}