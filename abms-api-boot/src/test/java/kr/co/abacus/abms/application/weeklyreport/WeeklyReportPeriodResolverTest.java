package kr.co.abacus.abms.application.weeklyreport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("주간 보고서 기간 계산")
class WeeklyReportPeriodResolverTest {

    @Test
    @DisplayName("weekStart와 weekEnd가 없으면 지난주 월요일부터 일요일을 계산한다")
    void resolveDefaultLastWeek() {
        WeeklyReportPeriodResolver.WeekRange range = WeeklyReportPeriodResolver.resolve(
                null,
                null,
                LocalDate.of(2026, 4, 3)
        );

        assertThat(range.weekStart()).isEqualTo(LocalDate.of(2026, 3, 23));
        assertThat(range.weekEnd()).isEqualTo(LocalDate.of(2026, 3, 29));
    }

    @Test
    @DisplayName("연말연초 경계에서도 지난주 월요일부터 일요일을 유지한다")
    void resolveDefaultAcrossYearBoundary() {
        WeeklyReportPeriodResolver.WeekRange range = WeeklyReportPeriodResolver.resolve(
                null,
                null,
                LocalDate.of(2026, 1, 2)
        );

        assertThat(range.weekStart()).isEqualTo(LocalDate.of(2025, 12, 22));
        assertThat(range.weekEnd()).isEqualTo(LocalDate.of(2025, 12, 28));
    }

    @Test
    @DisplayName("부분 입력은 허용하지 않는다")
    void rejectPartialRange() {
        assertThatThrownBy(() -> WeeklyReportPeriodResolver.resolve(
                LocalDate.of(2026, 3, 23),
                null,
                LocalDate.of(2026, 4, 3)
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
