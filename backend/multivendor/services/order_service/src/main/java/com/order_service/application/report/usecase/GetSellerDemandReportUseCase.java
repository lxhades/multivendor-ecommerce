package com.order_service.application.report.usecase;

import com.order_service.application.report.dto.SellerDemandReport;
import com.order_service.application.report.dto.SellerDemandReport.Totals;
import com.order_service.application.report.model.*;
import com.order_service.application.report.port.SellerReportQueryPort;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;

@Service
public class GetSellerDemandReportUseCase {
    private final SellerReportQueryPort reports;
    private final SellerReportAccess access;
    public GetSellerDemandReportUseCase(SellerReportQueryPort reports, SellerReportAccess access) {
        this.reports = reports; this.access = access;
    }

    public SellerDemandReport execute(UUID sellerId, UUID shopId, LocalDate from, LocalDate to, ReportGroupBy groupBy) {
        if (groupBy == null || groupBy == ReportGroupBy.YEAR) {
            throw new IllegalArgumentException("Báo cáo nhu cầu hỗ trợ DAY, WEEK hoặc MONTH");
        }
        var range = new ReportRange(from, to);
        access.requireOwner(sellerId, shopId);
        // Ownership check finishes before entering the database aggregation transaction.
        var rows = reports.demand(shopId, range, groupBy);
        Map<LocalDate, SellerReportQueryPort.DemandBucket> byDate = new HashMap<>();
        long orders = 0, cancelled = 0, quantity = 0, cancelledQuantity = 0;
        for (var row : rows) {
            byDate.put(row.periodStart(), row);
            orders = Math.addExact(orders, row.totalOrders());
            cancelled = Math.addExact(cancelled, row.cancelledOrders());
            quantity = Math.addExact(quantity, row.totalQuantity());
            cancelledQuantity = Math.addExact(cancelledQuantity, row.cancelledQuantity());
        }
        List<SellerDemandReport.Period> periods = new ArrayList<>();
        for (LocalDate start = groupBy.startOf(from); !start.isAfter(to); start = groupBy.next(start)) {
            var row = byDate.get(start);
            Totals totals = row == null ? Totals.of(0, 0, 0, 0)
                    : Totals.of(row.totalOrders(), row.cancelledOrders(), row.totalQuantity(), row.cancelledQuantity());
            periods.add(SellerDemandReport.Period.of(start, groupBy.next(start).minusDays(1), totals));
        }
        return new SellerDemandReport(shopId, from, to, groupBy, ReportRange.ZONE.getId(),
                Totals.of(orders, cancelled, quantity, cancelledQuantity), List.copyOf(periods));
    }
}
