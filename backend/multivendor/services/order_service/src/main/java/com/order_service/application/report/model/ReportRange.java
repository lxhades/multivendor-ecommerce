package com.order_service.application.report.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public record ReportRange(LocalDate from, LocalDate to) {
    public static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    public ReportRange {
        if (from == null || to == null || from.isAfter(to)) {
            throw new IllegalArgumentException("from và to là bắt buộc, from không được sau to");
        }
        if (from.getYear() < 1 || to.getYear() > 9998 || ChronoUnit.DAYS.between(from, to) > 3660) {
            throw new IllegalArgumentException("Khoảng báo cáo tối đa 3661 ngày, năm trong khoảng 1 đến 9998");
        }
    }

    public Instant startInclusive() { return from.atStartOfDay(ZONE).toInstant(); }
    public Instant endExclusive() { return to.plusDays(1).atStartOfDay(ZONE).toInstant(); }
}
