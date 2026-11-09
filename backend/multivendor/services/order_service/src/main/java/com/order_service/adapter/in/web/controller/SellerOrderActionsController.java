package com.order_service.adapter.in.web.controller;

import com.common.dto.response.ApiResponse;
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
@RequestMapping("/api/orders/seller")

public class SellerOrderActionsController {
    private final GetOrderUseCase getOrder;
    private final ListOrdersByCheckoutUseCase listByCheckout;
    private final CancelOrderUseCase cancelOrder;
    private final ShipOrderUseCase shipOrder;
    private final ListOrdersByShopUseCase listByShop;

    @GetMapping("/{id}")
    public ApiResponse<OrderResult> get(@PathVariable UUID id, Authentication actor) {
        return ApiResponse.success(getOrder.execute(id, UUID.fromString(actor.getName()), OrderRole.SELLER));
    }

    @GetMapping(params = "checkoutId")
    public ApiResponse<List<OrderResult>> listByCheckout(@RequestParam UUID checkoutId, Authentication actor) {
        return ApiResponse.success(listByCheckout.execute(checkoutId, UUID.fromString(actor.getName()), OrderRole.SELLER));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<OrderResult> cancel(@PathVariable UUID id, Authentication actor) {
        return ApiResponse.success("Hủy đơn hàng thành công",
                cancelOrder.execute(UUID.fromString(actor.getName()), id, OrderRole.SELLER));
    }

    @GetMapping(params = "shopId")
    public ApiResponse<List<OrderResult>> listByShop(@RequestParam UUID shopId, Authentication actor) {
        return ApiResponse.success(listByShop.execute(shopId, UUID.fromString(actor.getName()), OrderRole.SELLER));
    }

    @PostMapping("/{id}/ship")
    public ApiResponse<OrderResult> ship(@PathVariable UUID id, Authentication actor) {
        return ApiResponse.success("Đã gửi đơn hàng cho bên vận chuyển",
                shipOrder.execute(id, UUID.fromString(actor.getName()), OrderRole.SELLER));
    }
}
