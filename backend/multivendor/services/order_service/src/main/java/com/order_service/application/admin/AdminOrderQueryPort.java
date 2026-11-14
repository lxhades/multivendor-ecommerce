package com.order_service.application.admin;

import com.order_service.application.order.dto.OrderResult;
import com.order_service.application.report.model.ReportRange;
import com.order_service.domain.model.enums.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AdminOrderQueryPort {
    enum DateBasis { CREATED_AT, UPDATED_AT }
    enum Direction { ASC, DESC }
    record Filter(UUID orderId, UUID checkoutId, UUID shopId, UUID buyerId,
                  OrderStatus status, PaymentStatus paymentStatus, PaymentMethod paymentMethod,
                  LocalDate from, LocalDate to, DateBasis dateBasis, Direction direction, String q,
                  int page, int size) {
        public Filter {
            validatePage(page, size);
            if (dateBasis == null || direction == null) throw new IllegalArgumentException("Thiếu kiểu ngày hoặc sắp xếp");
            if (from != null && (from.getYear() < 1 || from.getYear() > 9998)
                    || to != null && (to.getYear() < 1 || to.getYear() > 9998)
                    || from != null && to != null && from.isAfter(to)) throw new IllegalArgumentException("Khoảng ngày không hợp lệ");
            q = q == null ? "" : q.trim();
            if (q.length() > 150) throw new IllegalArgumentException("Từ khóa tối đa 150 ký tự");
        }
    }
    record OrderPage(List<OrderResult> items, int page, int size, long totalElements, long totalPages) {}
    record Amounts(long deliveredOrders, long paidOrders, BigDecimal revenue, BigDecimal shippingFee,
                   BigDecimal totalOrderAmount, BigDecimal paidOrderAmount, BigDecimal unpaidOrderAmount) {}
    record ShopRevenue(UUID shopId, Amounts summary) {}
    record RevenueReport(LocalDate from, LocalDate to, String timezone, String dateBasis,
                         Amounts summary, List<ShopRevenue> items, int page, int size,
                         long totalElements, long totalPages) {}
    OrderPage orders(Filter filter);
    RevenueReport revenue(ReportRange range, UUID shopId, int page, int size);
    static void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100 || (long) page * size > Integer.MAX_VALUE)
            throw new IllegalArgumentException("page phải >= 0, size từ 1 đến 100 và offset không vượt giới hạn");
    }
}
