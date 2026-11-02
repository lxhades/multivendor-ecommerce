package com.inventory_service.adapter.out.stockreservation.persistence;

import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import com.inventory_service.adapter.out.stockreservation.persistence.entity.StockReservationJpaEntity;
import com.inventory_service.adapter.out.stockreservation.persistence.mapper.StockReservationPersistenceMapper;
import com.inventory_service.adapter.out.stockreservation.persistence.repository.StockReservationJpaRepository;
import com.inventory_service.domain.model.entity.StockReservation;
import com.inventory_service.domain.model.enums.ReservationStatus;
import com.inventory_service.domain.repository.StockReservationRepository;

import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class StockReservationPersistenceAdapter
        implements StockReservationRepository {

    private final StockReservationJpaRepository jpaRepository;
    private final StockReservationPersistenceMapper mapper;
    private final EntityManager entityManager;


    @Override
    @Transactional
    public StockReservation create(StockReservation reservation) {

        if (reservation.getVersion() != null) {
            throw new IllegalArgumentException(
                    "New reservation must not have a version"
            );
        }

        StockReservationJpaEntity entity =
                mapper.toJpaEntity(reservation);

        entityManager.persist(entity);
        entityManager.flush();

        return mapper.toDomain(entity);
    }
    @Override
    @Transactional
    public StockReservation update(StockReservation reservation) {

        StockReservationJpaEntity entity =
                jpaRepository.findById(reservation.getId())
                        .orElseThrow(() ->
                                new DomainException(
                                        ErrorCode.STOCK_RESERVATION_NOT_FOUND
                                )
                        );

        if (reservation.getVersion() == null ||
                !reservation.getVersion().equals(entity.getVersion())) {

            throw new OptimisticLockingFailureException(
                    "StockReservation version conflict"
            );
        }

        if (entity.getStatus() != ReservationStatus.RESERVED) {
            throw new IllegalStateException(
                    "Reservation is no longer active"
            );
        }

        if (reservation.getStatus() == ReservationStatus.RESERVED) {
            throw new IllegalStateException(
                    "No valid status transition"
            );
        }

        entity.updateStatus(
                reservation.getStatus(),
                reservation.getUpdatedAt()
        );

        entityManager.flush();

        return mapper.toDomain(entity);
    }
    @Override
    public Optional<StockReservation> findById(UUID reservationId) {
        return jpaRepository.findById(reservationId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<StockReservation> findByOrderIdAndInventoryId(
            UUID orderId,
            UUID inventoryId
    ) {
        return jpaRepository
                .findByOrderIdAndInventoryId(orderId, inventoryId)
                .map(mapper::toDomain);
    }

    @Override
    public List<StockReservation> findByOrderId(UUID orderId) {
        return jpaRepository.findByOrderId(orderId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}