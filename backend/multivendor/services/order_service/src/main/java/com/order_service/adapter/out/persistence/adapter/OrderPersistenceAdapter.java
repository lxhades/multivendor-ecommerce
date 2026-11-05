package com.order_service.adapter.out.persistence.adapter;

import com.order_service.adapter.out.persistence.entity.OrderJpaEntity;
import com.order_service.adapter.out.persistence.jpa.SpringOrderJpaRepository;
import com.order_service.adapter.out.persistence.mapper.OrderJpaMapper;
import com.order_service.domain.model.aggregate.Order;
import com.order_service.domain.repository.OrderRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class OrderPersistenceAdapter implements OrderRepository {

    private final SpringOrderJpaRepository jpa;

    public OrderPersistenceAdapter(SpringOrderJpaRepository jpa) {
        this.jpa = jpa;
    }


    public Order save(Order order) {
        OrderJpaEntity source = OrderJpaMapper.toJpa(order);
        OrderJpaEntity target = jpa.findById(order.getId()).orElse(null);

        if (target == null) {
            return OrderJpaMapper.toDomain(jpa.save(source));
        }

        if (!Objects.equals(target.version, order.getVersion())) {
            throw new org.springframework.orm.ObjectOptimisticLockingFailureException(
                    OrderJpaEntity.class,
                    order.getId()
            );
        }

        target.orderStatus = source.orderStatus;
        target.paymentStatus = source.paymentStatus;
        target.updatedAt = source.updatedAt;

        return OrderJpaMapper.toDomain(jpa.save(target));
    }

    public List<Order> saveAll(List<Order> orders) {
        List<Order> result = new ArrayList<>();

        for (Order o : orders) {
            result.add(save(o));
        }

        return result;
    }

    public Optional<Order> findById(UUID id) {
        return jpa.findById(id)
                .map(OrderJpaMapper::toDomain);
    }

    public Optional<Order> findByCheckoutIdAndShopId(UUID checkoutId, UUID shopId) {
        return jpa.findByCheckoutIdAndShopId(checkoutId, shopId)
                .map(OrderJpaMapper::toDomain);
    }

    public List<Order> findByCheckoutId(UUID id) {
        return jpa.findByCheckoutId(id)
                .stream()
                .map(OrderJpaMapper::toDomain)
                .toList();
    }

    public List<Order> findByBuyerId(UUID id) {
        return jpa.findByBuyerId(id)
                .stream()
                .map(OrderJpaMapper::toDomain)
                .toList();
    }

    public List<Order> findByShopId(UUID id) {
        return jpa.findByShopId(id)
                .stream()
                .map(OrderJpaMapper::toDomain)
                .toList();
    }
}