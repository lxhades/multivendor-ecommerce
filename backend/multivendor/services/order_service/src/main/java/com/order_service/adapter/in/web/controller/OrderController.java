package com.order_service.adapter.in.web.controller;

import com.common.dto.response.ApiResponse;
import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import com.order_service.application.order.OrderRole;
import com.order_service.application.order.dto.OrderResult;
import com.order_service.application.order.usecase.*;
import com.order_service.application.order.usecase.flow.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")

public class OrderController {
    private final GetOrderUseCase getOrder;
    private final ListOrdersByCheckoutUseCase listByCheckout;
    private final CancelOrderUseCase cancelOrder;
    private final ListOrdersByBuyerUseCase listByBuyer;

    @GetMapping("/{id}")
    public ApiResponse<OrderResult> get(@PathVariable UUID id, Authentication actor) {
        return ApiResponse.success(getOrder.execute(id, UUID.fromString(actor.getName()), OrderRole.BUYER));
    }

    @GetMapping(params = "checkoutId")
    public ApiResponse<List<OrderResult>> listByCheckout(@RequestParam UUID checkoutId, Authentication actor) {
        return ApiResponse.success(listByCheckout.execute(checkoutId, UUID.fromString(actor.getName()), OrderRole.BUYER));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<OrderResult> cancel(@PathVariable UUID id, Authentication actor) {
        return ApiResponse.success("Hủy đơn hàng thành công",
                cancelOrder.execute(UUID.fromString(actor.getName()), id, OrderRole.BUYER));
    }

    @GetMapping
    public ApiResponse<List<OrderResult>> list(@RequestParam(required = false) UUID buyerId, Authentication actor) {
        UUID currentBuyer = UUID.fromString(actor.getName());
        if (buyerId != null && !buyerId.equals(currentBuyer)) {
            throw new DomainException(ErrorCode.ACCESS_DENIED);
        }
        return ApiResponse.success(listByBuyer.execute(currentBuyer));
    }
}
