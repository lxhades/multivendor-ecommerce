package com.order_service.adapter.in.web.controller;

import com.common.dto.response.ApiResponse;
import com.order_service.application.report.dto.*;
import com.order_service.application.report.model.ReportGroupBy;
import com.order_service.application.report.usecase.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders/seller/shops/{shopId}/reports")
public class SellerReportController {
    private final GetSellerDemandReportUseCase demand;
    private final GetSellerSalesReportUseCase sales;
    public SellerReportController(GetSellerDemandReportUseCase demand, GetSellerSalesReportUseCase sales) {
        this.demand = demand; this.sales = sales;
    }

    @GetMapping("/demand")
    public ApiResponse<SellerDemandReport> demand(Authentication actor, @PathVariable UUID shopId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "DAY") ReportGroupBy groupBy) {
        return ApiResponse.success("Lấy báo cáo đặt mua và hủy đơn thành công",
                demand.execute(sellerId(actor), shopId, from, to, groupBy));
    }

    @GetMapping("/sales")
    public ApiResponse<SellerSalesReport> sales(Authentication actor, @PathVariable UUID shopId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "MONTH") ReportGroupBy groupBy,
            @RequestParam(defaultValue = "0") int variantPage,
            @RequestParam(defaultValue = "20") int variantSize) {
        return ApiResponse.success("Lấy báo cáo doanh thu và sản phẩm bán thành công",
                sales.execute(sellerId(actor), shopId, from, to, groupBy, variantPage, variantSize));
    }

    private UUID sellerId(Authentication actor) {
        try {
            if (actor == null || actor.getName() == null) throw new IllegalArgumentException();
            return UUID.fromString(actor.getName());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT không có subject người bán hợp lệ");
        }
    }
}
