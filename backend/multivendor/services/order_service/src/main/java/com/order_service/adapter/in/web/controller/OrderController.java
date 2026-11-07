package com.order_service.adapter.in.web.controller;

import com.common.dto.response.ApiResponse;
import com.order_service.application.order.dto.OrderResult;
import com.order_service.application.order.usecase.GetOrderUseCase;
import com.order_service.application.order.usecase.ListOrdersByBuyerUseCase;
import com.order_service.application.order.usecase.ListOrdersByCheckoutUseCase;
import com.order_service.application.order.usecase.ListOrdersByShopUseCase;
import com.order_service.application.order.usecase.flow.*;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final GetOrderUseCase getOrderUseCase;
    private final ListOrdersByBuyerUseCase listOrdersByBuyerUseCase;
    private final ListOrdersByShopUseCase listOrdersByShopUseCase;
    private final ListOrdersByCheckoutUseCase listOrdersByCheckoutUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final ConfirmCodOrderUseCase confirmCodOrderUseCase;
    private final StartProcessingOrderUseCase startProcessingOrderUseCase;
    private final ShipOrderUseCase shipOrderUseCase;
    private final DeliverOrderUseCase deliverOrderUseCase;


    @GetMapping("/{id}")
    public ApiResponse<OrderResult> getById(@PathVariable UUID id) {
        return ApiResponse.success(
                "Lấy thông tin đơn hàng thành công",
                getOrderUseCase.execute(id)
        );
    }

    @GetMapping(params = "buyerId")
    public ApiResponse<List<OrderResult>> listByBuyer(@RequestParam UUID buyerId) {
        return ApiResponse.success(
                "Lấy danh sách đơn hàng theo người mua thành công",
                listOrdersByBuyerUseCase.execute(buyerId)
        );
    }

    @GetMapping(params = "shopId")
    public ApiResponse<List<OrderResult>> listByShop(@RequestParam UUID shopId) {
        return ApiResponse.success(
                "Lấy danh sách đơn hàng theo shop thành công",
                listOrdersByShopUseCase.execute(shopId)
        );
    }

    @GetMapping(params = "checkoutId")
    public ApiResponse<List<OrderResult>> listByCheckout(@RequestParam UUID checkoutId) {
        return ApiResponse.success(
                "Lấy danh sách đơn hàng theo checkout thành công",
                listOrdersByCheckoutUseCase.execute(checkoutId)
        );
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<OrderResult> cancel(@PathVariable UUID orderId,
                                           @AuthenticationPrincipal Jwt jwt
    ) {
        UUID buyerId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(
                "Hủy đơn hàng thành công",
                cancelOrderUseCase.execute(buyerId, orderId)
        );
    }

    @PostMapping("/{id}/confirm-cod")
    public ApiResponse<OrderResult> confirmCod(@PathVariable UUID id) {
        return ApiResponse.success(
                "Xác nhận đơn hàng COD thành công",
                confirmCodOrderUseCase.execute(id)
        );
    }

    @PostMapping("/{id}/processing")
    public ApiResponse<OrderResult> startProcessing(@PathVariable UUID id) {
        return ApiResponse.success(
                "Bắt đầu xử lý đơn hàng thành công",
                startProcessingOrderUseCase.execute(id)
        );
    }

    @PostMapping("/{id}/ship")
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<OrderResult> ship(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID sellerId = UUID.fromString(jwt.getSubject());

        return ApiResponse.success(
                "Đã gửi đơn hàng cho Bên Ship",
                shipOrderUseCase.execute(id, sellerId)
        );
    }

    @PostMapping("/{id}/deliver")
    public ApiResponse<OrderResult> deliver(@PathVariable UUID id) {
        return ApiResponse.success(
                "Xác nhận giao hàng thành công",
                deliverOrderUseCase.execute(id)
        );
    }
}