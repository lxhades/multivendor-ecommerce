package com.order_service.adapter.in.web.controller;

import com.common.dto.response.ApiResponse;
import com.order_service.application.order.OrderRole;
import com.order_service.application.order.dto.OrderResult;

import com.order_service.application.order.usecase.GetOrderUseCase;
import com.order_service.application.order.usecase.ListOrdersByBuyerUseCase;
import com.order_service.application.order.usecase.ListOrdersByCheckoutUseCase;
import com.order_service.application.order.usecase.ListOrdersByShopUseCase;

import com.order_service.application.order.usecase.flow.CancelOrderUseCase;
import com.order_service.application.order.usecase.flow.ShipOrderUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders/admin")
public class AdminOrderController {

    private static final OrderRole ACTOR_ROLE = OrderRole.ADMIN;
    private static final String CANCEL_SUCCESS_MESSAGE = "Hủy đơn hàng thành công";
    private static final String SHIP_SUCCESS_MESSAGE = "Đã gửi đơn hàng cho bên vận chuyển";

    private final GetOrderUseCase getOrderUseCase;
    private final ListOrdersByCheckoutUseCase listOrdersByCheckoutUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final ShipOrderUseCase shipOrderUseCase;
    private final ListOrdersByShopUseCase listOrdersByShopUseCase;
    private final ListOrdersByBuyerUseCase listOrdersByBuyerUseCase;

    @GetMapping("/{orderId}")
    public ApiResponse<OrderResult> getOrder(
            @PathVariable UUID orderId,
            Authentication authentication
    ) {
        return ApiResponse.success(
                getOrderUseCase.execute(orderId, currentAdminId(authentication), ACTOR_ROLE)
        );
    }

    @GetMapping(params = "checkoutId")
    public ApiResponse<List<OrderResult>> listOrdersByCheckout(
            @RequestParam UUID checkoutId,
            Authentication authentication
    ) {
        return ApiResponse.success(
                listOrdersByCheckoutUseCase.execute(checkoutId, currentAdminId(authentication), ACTOR_ROLE)
        );
    }

    @GetMapping(params = "shopId")
    public ApiResponse<List<OrderResult>> listOrdersByShop(
            @RequestParam UUID shopId,
            Authentication authentication
    ) {
        return ApiResponse.success(
                listOrdersByShopUseCase.execute(shopId, currentAdminId(authentication), ACTOR_ROLE)
        );
    }

    @GetMapping(params = "buyerId")
    public ApiResponse<List<OrderResult>> listOrdersByBuyer(@RequestParam UUID buyerId) {
        return ApiResponse.success(listOrdersByBuyerUseCase.execute(buyerId));
    }

    @PostMapping("/{orderId}/cancel")
    public ApiResponse<OrderResult> cancelOrder(
            @PathVariable UUID orderId,
            Authentication authentication
    ) {
        return ApiResponse.success(
                CANCEL_SUCCESS_MESSAGE,
                cancelOrderUseCase.execute(currentAdminId(authentication), orderId, ACTOR_ROLE)
        );
    }

    @PostMapping("/{orderId}/ship")
    public ApiResponse<OrderResult> shipOrder(
            @PathVariable UUID orderId,
            Authentication authentication
    ) {
        return ApiResponse.success(
                SHIP_SUCCESS_MESSAGE,
                shipOrderUseCase.execute(orderId, currentAdminId(authentication), ACTOR_ROLE)
        );
    }

    private UUID currentAdminId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}