package kr.co.abacus.abms.application.weeklyreport;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import org.jspecify.annotations.Nullable;

final class WeeklyReportPeriodResolver {

    private WeeklyReportPeriodResolver() {
    }

    static WeekRange resolve(@Nullable LocalDate weekStart, @Nullable LocalDate weekEnd, LocalDate today) {
        if (weekStart == null && weekEnd == null) {
            LocalDate lastWeekReference = today.minusWeeks(1);
            LocalDate resolvedStart = lastWeekReference.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate resolvedEnd = resolvedStart.plusDays(6);
            return new WeekRange(resolvedStart, resolvedEnd);
        }

        if (weekStart == null || weekEnd == null) {
            throw new IllegalArgumentException("weekStart와 weekEnd는 함께 전달되어야 합니다.");
        }
        if (weekStart.isAfter(weekEnd)) {
            throw new IllegalArgumentException("weekStart는 weekEnd보다 늦을 수 없습니다.");
        }
        if (!weekStart.getDayOfWeek().equals(DayOfWeek.MONDAY) || !weekEnd.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            throw new IllegalArgumentException("주간 보고서는 월요일부터 일요일까지의 범위만 지원합니다.");
        }
        if (!weekStart.plusDays(6).equals(weekEnd)) {
            throw new IllegalArgumentException("주간 보고서 범위는 정확히 7일이어야 합니다.");
        }

        return new WeekRange(weekStart, weekEnd);
    }

    record WeekRange(LocalDate weekStart, LocalDate weekEnd) {

        WeekRange previousWeek() {
            return new WeekRange(weekStart.minusWeeks(1), weekEnd.minusWeeks(1));
        }
    }
}
