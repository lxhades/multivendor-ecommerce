package com.order_service.application.report.model;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;

public enum ReportGroupBy {
    DAY, WEEK, MONTH, YEAR;

    public String sqlUnit() { return name().toLowerCase(Locale.ROOT); }

    public LocalDate startOf(LocalDate date) {
        return switch (this) {
            case DAY -> date;
            case WEEK -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTH -> date.withDayOfMonth(1);
            case YEAR -> date.withDayOfYear(1);
        };
    }

    public LocalDate next(LocalDate start) {
        return switch (this) {
            case DAY -> start.plusDays(1);
            case WEEK -> start.plusWeeks(1);
            case MONTH -> start.plusMonths(1);
            case YEAR -> start.plusYears(1);
        };
    }
}
