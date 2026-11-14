package com.order_service.application.admin;

import com.order_service.application.report.model.ReportRange;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class AdminOrderQueryUseCase {
    private final AdminOrderQueryPort query;
    public AdminOrderQueryUseCase(AdminOrderQueryPort query) { this.query = query; }
    public AdminOrderQueryPort.OrderPage orders(AdminOrderQueryPort.Filter filter) { return query.orders(filter); }
    public AdminOrderQueryPort.RevenueReport revenue(LocalDate from, LocalDate to, UUID shopId, int page, int size) {
        AdminOrderQueryPort.validatePage(page, size);
        return query.revenue(new ReportRange(from, to), shopId, page, size);
    }
}
