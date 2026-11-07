package com.inventory_service.adapter.out.stockreservation.persistence.mapper;

import com.inventory_service.adapter.out.stockreservation.persistence.entity.StockReservationJpaEntity;
import com.inventory_service.domain.model.entity.StockReservation;

import org.springframework.stereotype.Component;

@Component
public class StockReservationPersistenceMapper {

    public StockReservation toDomain(
            StockReservationJpaEntity entity
    ) {
        return StockReservation.restore(
                entity.getId(),
                entity.getOrderId(),
                entity.getInventoryId(),
                entity.getQuantity(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }

    public StockReservationJpaEntity toJpaEntity(
            StockReservation reservation
    ) {
        return new StockReservationJpaEntity(
                reservation.getId(),
                reservation.getOrderId(),
                reservation.getInventoryId(),
                reservation.getQuantity(),
                reservation.getStatus(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt()
        );
    }
}