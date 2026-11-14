package com.order_service.adapter.in.web.controller;

import com.common.dto.response.ApiResponse;
import com.order_service.application.admin.*;
import com.order_service.application.admin.AdminOrderQueryPort.*;
import com.order_service.domain.model.enums.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders/admin")
public class AdminOrderQueryController {
    private final AdminOrderQueryUseCase useCase;
    public AdminOrderQueryController(AdminOrderQueryUseCase useCase) { this.useCase = useCase; }

    @GetMapping("/page")
    public ApiResponse<OrderPage> orders(
            @RequestParam(required=false) UUID orderId, @RequestParam(required=false) UUID checkoutId,
            @RequestParam(required=false) UUID shopId, @RequestParam(required=false) UUID buyerId,
            @RequestParam(required=false) OrderStatus status, @RequestParam(required=false) PaymentStatus paymentStatus,
            @RequestParam(required=false) PaymentMethod paymentMethod,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue="CREATED_AT") DateBasis dateBasis,
            @RequestParam(defaultValue="DESC") Direction direction, @RequestParam(defaultValue="") String q,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        return ApiResponse.success(useCase.orders(new Filter(orderId, checkoutId, shopId, buyerId, status,
                paymentStatus, paymentMethod, from, to, dateBasis, direction, q, page, size)));
    }
    @GetMapping("/reports/revenue")
    public ApiResponse<RevenueReport> revenue(
            @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required=false) UUID shopId,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="10") int size) {
        return ApiResponse.success(useCase.revenue(from, to, shopId, page, size));
    }
}
