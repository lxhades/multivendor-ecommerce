package com.order_service.application.report.dto;

import com.order_service.application.report.model.ReportGroupBy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SellerSalesReport(UUID shopId, LocalDate from, LocalDate to, ReportGroupBy groupBy,
                                String timezone, String dateBasis, Totals summary, List<Period> periods,
                                List<SoldVariant> bestSellingVariants, int variantPage, int variantSize,
                                long totalVariants, long totalVariantPages) {
    public record Totals(long deliveredOrders, long soldQuantity, BigDecimal revenue,
                         BigDecimal shippingFee, BigDecimal totalOrderAmount) {}
    public record Period(LocalDate periodStart, LocalDate periodEnd, long deliveredOrders, long soldQuantity,
                         BigDecimal revenue, BigDecimal shippingFee, BigDecimal totalOrderAmount) {
        public static Period of(LocalDate start, LocalDate end, Totals t) {
            return new Period(start, end, t.deliveredOrders(), t.soldQuantity(), t.revenue(), t.shippingFee(), t.totalOrderAmount());
        }
    }
    public record SoldVariant(UUID productId, String productName, UUID variantId, String variantName,
                              long soldQuantity, BigDecimal revenue) {}
}
