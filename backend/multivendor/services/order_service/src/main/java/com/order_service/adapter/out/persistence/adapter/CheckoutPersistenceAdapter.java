package com.order_service.adapter.out.persistence.adapter;

import com.order_service.adapter.out.persistence.jpa.SpringCheckoutJpaRepository;
import com.order_service.adapter.out.persistence.mapper.CheckoutJpaMapper;
import com.order_service.domain.model.aggregate.Checkout;
import com.order_service.domain.repository.CheckoutRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class CheckoutPersistenceAdapter implements CheckoutRepository {

    private final SpringCheckoutJpaRepository jpa;

    public CheckoutPersistenceAdapter(SpringCheckoutJpaRepository jpa) {
        this.jpa = jpa;
    }

    public Checkout save(Checkout c) {
        return CheckoutJpaMapper.toDomain(
                jpa.save(CheckoutJpaMapper.toJpa(c))
        );
    }

    public Optional<Checkout> findById(UUID id) {
        return jpa.findById(id)
                .map(CheckoutJpaMapper::toDomain);
    }

    public Optional<Checkout> findByBuyerIdAndIdempotencyKey(UUID buyer, String key) {
        return jpa.findByBuyerIdAndIdempotencyKey(buyer, key)
                .map(CheckoutJpaMapper::toDomain);
    }
}