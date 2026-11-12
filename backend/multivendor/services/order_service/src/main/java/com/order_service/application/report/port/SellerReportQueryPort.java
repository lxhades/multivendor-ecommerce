package com.order_service.application.report.port;

import com.order_service.application.report.dto.SellerSalesReport.SoldVariant;
import com.order_service.application.report.model.ReportGroupBy;
import com.order_service.application.report.model.ReportRange;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SellerReportQueryPort {
    List<DemandBucket> demand(UUID shopId, ReportRange range, ReportGroupBy groupBy);
    SalesData sales(UUID shopId, ReportRange range, ReportGroupBy groupBy, int variantPage, int variantSize);

    record DemandBucket(LocalDate periodStart, long totalOrders, long cancelledOrders,
                        long totalQuantity, long cancelledQuantity) {}
    record SalesBucket(LocalDate periodStart, long deliveredOrders, long soldQuantity,
                       BigDecimal revenue, BigDecimal shippingFee, BigDecimal totalOrderAmount) {}
    record SalesData(List<SalesBucket> buckets, List<SoldVariant> variants, long totalVariants) {}
}
