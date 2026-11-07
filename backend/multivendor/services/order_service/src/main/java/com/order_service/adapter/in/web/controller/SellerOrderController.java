package com.order_service.adapter.in.web.controller;

import com.common.dto.response.ApiResponse;
import com.order_service.application.order.usecase.ListSellerOrdersUseCase;
import com.order_service.domain.model.enums.OrderStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/seller/shops/{shopId}/orders")
@PreAuthorize("hasRole('SELLER')")
public class SellerOrderController {
    private final ListSellerOrdersUseCase listOrders;

    public SellerOrderController(ListSellerOrdersUseCase listOrders) {
        this.listOrders = listOrders;
    }

    @GetMapping
    public ApiResponse<ListSellerOrdersUseCase.Result> list(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) OrderStatus status
    ) {
        return ApiResponse.success("Lấy danh sách đơn hàng thành công",
                listOrders.execute(UUID.fromString(jwt.getSubject()), shopId, page, size, from, to, status));
    }
}
