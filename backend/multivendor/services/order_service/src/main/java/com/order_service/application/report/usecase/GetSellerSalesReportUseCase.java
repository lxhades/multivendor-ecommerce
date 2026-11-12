package com.order_service.application.report.usecase;

import com.order_service.application.report.dto.SellerSalesReport;
import com.order_service.application.report.dto.SellerSalesReport.Totals;
import com.order_service.application.report.model.*;
import com.order_service.application.report.port.SellerReportQueryPort;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class GetSellerSalesReportUseCase {
    private final SellerReportQueryPort reports;
    private final SellerReportAccess access;
    public GetSellerSalesReportUseCase(SellerReportQueryPort reports, SellerReportAccess access) {
        this.reports = reports; this.access = access;
    }

    public SellerSalesReport execute(UUID sellerId, UUID shopId, LocalDate from, LocalDate to,
                                    ReportGroupBy groupBy, int variantPage, int variantSize) {
        if (groupBy == null) {
            throw new IllegalArgumentException("Báo cáo doanh thu hỗ trợ DAY, WEEK, MONTH hoặc YEAR");
        }
        if (variantPage < 0 || variantSize < 1 || variantSize > 100 || (long) variantPage * variantSize > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("variantPage phải >= 0, variantSize từ 1 đến 100 và offset không vượt Integer.MAX_VALUE");
        }
        var range = new ReportRange(from, to);
        access.requireOwner(sellerId, shopId);
        var data = reports.sales(shopId, range, groupBy, variantPage, variantSize);
        Map<LocalDate, SellerReportQueryPort.SalesBucket> byDate = new HashMap<>();
        long deliveredOrders = 0, soldQuantity = 0;
        BigDecimal revenue = BigDecimal.ZERO, shippingFee = BigDecimal.ZERO, totalAmount = BigDecimal.ZERO;
        for (var row : data.buckets()) {
            byDate.put(row.periodStart(), row);
            deliveredOrders = Math.addExact(deliveredOrders, row.deliveredOrders());
            soldQuantity = Math.addExact(soldQuantity, row.soldQuantity());
            revenue = revenue.add(row.revenue()); shippingFee = shippingFee.add(row.shippingFee());
            totalAmount = totalAmount.add(row.totalOrderAmount());
        }
        List<SellerSalesReport.Period> periods = new ArrayList<>();
        for (LocalDate start = groupBy.startOf(from); !start.isAfter(to); start = groupBy.next(start)) {
            var row = byDate.get(start);
            Totals totals = row == null ? new Totals(0, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)
                    : new Totals(row.deliveredOrders(), row.soldQuantity(), row.revenue(), row.shippingFee(), row.totalOrderAmount());
            periods.add(SellerSalesReport.Period.of(start, groupBy.next(start).minusDays(1), totals));
        }
        long pages = data.totalVariants() / variantSize + (data.totalVariants() % variantSize == 0 ? 0 : 1);
        return new SellerSalesReport(shopId, from, to, groupBy, ReportRange.ZONE.getId(), "updatedAt",
                new Totals(deliveredOrders, soldQuantity, revenue, shippingFee, totalAmount), List.copyOf(periods),
                List.copyOf(data.variants()), variantPage, variantSize, data.totalVariants(), pages);
    }
}
