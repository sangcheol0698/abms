package kr.co.abacus.abms.common.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import jakarta.persistence.Embeddable;

import org.jspecify.annotations.Nullable;

/**
 * 시작일과 (선택적) 종료일로 이루어진 기간. 종료일이 없으면 진행 중으로 본다.
 */
@Embeddable
public record Period(LocalDate startDate, @Nullable LocalDate endDate) {

    public Period {
        Objects.requireNonNull(startDate, "시작일은 필수입니다.");
        if (endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException("시작일은 종료일보다 늦을 수 없습니다.");
        }
    }

    public boolean contains(LocalDate date) {
        return !date.isBefore(startDate) && (endDate == null || !date.isAfter(endDate));
    }

    public boolean overlaps(LocalDate from, LocalDate to) {
        return !startDate.isAfter(to) && (endDate == null || !endDate.isBefore(from));
    }

    public boolean overlaps(YearMonth month) {
        return overlaps(month.atDay(1), month.atEndOfMonth());
    }

    /**
     * 해당 월 중 이 기간에 속한 비율(M/M). 월 총일수 대비 일수 (소수 첫째 자리 반올림).
     * 예: 2월(28일) 중 14일 → 0.5
     */
    public BigDecimal manMonth(YearMonth month) {
        LocalDate monthStart = month.atDay(1);
        LocalDate monthEnd = month.atEndOfMonth();

        LocalDate realStart = startDate.isAfter(monthStart) ? startDate : monthStart;
        LocalDate realEnd = endDate != null && endDate.isBefore(monthEnd) ? endDate : monthEnd;
        if (realStart.isAfter(realEnd)) {
            return BigDecimal.ZERO;
        }
        long days = ChronoUnit.DAYS.between(realStart, realEnd) + 1;
        return BigDecimal.valueOf(days)
                .divide(BigDecimal.valueOf(month.lengthOfMonth()), 1, RoundingMode.HALF_UP);
    }

    public boolean isOpenEnded() {
        return endDate == null;
    }

}
