package com.order_service.adapter.out.persistence.adapter;

import com.order_service.adapter.out.persistence.entity.OrderJpaEntity;
import com.order_service.adapter.out.persistence.jpa.SpringOrderJpaRepository;
import com.order_service.adapter.out.persistence.mapper.OrderJpaMapper;
import com.order_service.domain.model.aggregate.Order;
import com.order_service.domain.model.enums.OrderStatus;
import com.order_service.domain.repository.OrderRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.Instant;
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

    @Override
    public Page<Order> findShopOrders(UUID shopId, OrderStatus status, Instant from, Instant until, Pageable page) {

        // Tạo Specification để lọc động
        Specification<OrderJpaEntity> spec = (root, query, cb) -> {
            List<Predicate> filters = new ArrayList<>();

            // Bắt buộc: Lọc theo cửa hàng
            filters.add(cb.equal(root.get("shopId"), shopId));

            // Tùy chọn: Lọc theo trạng thái đơn hàng (Truyền trực tiếp Enum)
            if (status != null) {
                filters.add(cb.equal(root.get("orderStatus"), status));
            }

            // Tùy chọn: Lọc theo khoảng thời gian (Từ ngày)
            if (from != null) {
                filters.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }

            // Tùy chọn: Lọc theo khoảng thời gian (Đến ngày)
            if (until != null) {
                filters.add(cb.lessThan(root.get("createdAt"), until));
            }

            // Kết hợp tất cả điều kiện bằng AND
            return cb.and(filters.toArray(new Predicate[0]));
        };

        // Thực thi query và map sang Domain
        return jpa.findAll(spec, page).map(OrderJpaMapper::toDomain);
    }
}
