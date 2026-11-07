package com.order_service.application.order.usecase;

import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import com.order_service.application.checkout.port.ShopOwnershipPort;
import com.order_service.application.order.dto.OrderResult;
import com.order_service.domain.model.enums.OrderStatus;
import com.order_service.domain.repository.OrderRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class ListSellerOrdersUseCase {

    private static final ZoneId SHOP_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final OrderRepository orderRepository;
    private final ShopOwnershipPort shopOwnershipPort;

    public ListSellerOrdersUseCase(OrderRepository orderRepository, ShopOwnershipPort shopOwnershipPort) {
        this.orderRepository = orderRepository;
        this.shopOwnershipPort = shopOwnershipPort;
    }


    @Transactional(readOnly = true)
    public Result execute(UUID sellerId, UUID shopId, int page, int size,
                          LocalDate from, LocalDate to, OrderStatus status) {

        if (page < 0 || size < 1 || size > 100 || (long) page * size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Trang phải hợp lệ và kích thước trang từ 1 đến 100");
        }
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("Ngày bắt đầu không được sau ngày kết thúc");
        }

        if (!shopOwnershipPort.isOwner(sellerId, shopId)) {
            throw new DomainException(ErrorCode.ACCESS_DENIED);
        }

        // Chuyển đổi LocalDate sang Instant (áp dụng múi giờ VN)
        Instant start = from == null ? null : from.atStartOfDay(SHOP_ZONE).toInstant();
        Instant until = to == null ? null : to.plusDays(1).atStartOfDay(SHOP_ZONE).toInstant();

        var pageOfOrders = orderRepository.findShopOrders(
                shopId,
                status,
                start,
                until,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id"))
        );

        var items = pageOfOrders.map(order -> new Item(
                OrderResult.from(order),
                order.getCreatedAt(),
                order.getUpdatedAt()
        ));

        return new Result(
                items.getContent(),
                page,
                size,
                items.getTotalElements(),
                items.getTotalPages()
        );
    }

    public record Item(
            OrderResult order,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record Result(
            List<Item> items,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {}
}