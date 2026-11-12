package com.order_service.application.report.dto;

import com.order_service.application.report.model.ReportGroupBy;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SellerDemandReport(UUID shopId, LocalDate from, LocalDate to, ReportGroupBy groupBy,
                                 String timezone, Totals summary, List<Period> periods) {
    public record Totals(long totalOrders, long nonCancelledOrders, long cancelledOrders,
                         long totalQuantity, long nonCancelledQuantity, long cancelledQuantity,
                         BigDecimal cancellationRate) {
        public static Totals of(long orders, long cancelled, long quantity, long cancelledQuantity) {
            BigDecimal rate = orders == 0 ? BigDecimal.ZERO.setScale(2)
                    : BigDecimal.valueOf(cancelled).multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(orders), 2, RoundingMode.HALF_UP);
            return new Totals(orders, orders - cancelled, cancelled, quantity, quantity - cancelledQuantity, cancelledQuantity, rate);
        }
    }
    public record Period(LocalDate periodStart, LocalDate periodEnd, long totalOrders,
                         long nonCancelledOrders, long cancelledOrders, long totalQuantity,
                         long nonCancelledQuantity, long cancelledQuantity, BigDecimal cancellationRate) {
        public static Period of(LocalDate start, LocalDate end, Totals t) {
            return new Period(start, end, t.totalOrders(), t.nonCancelledOrders(), t.cancelledOrders(),
                    t.totalQuantity(), t.nonCancelledQuantity(), t.cancelledQuantity(), t.cancellationRate());
        }
    }
}
