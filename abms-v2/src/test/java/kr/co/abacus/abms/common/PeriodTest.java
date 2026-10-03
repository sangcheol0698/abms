package kr.co.abacus.abms.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.YearMonth;

import org.junit.jupiter.api.Test;

import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.Period;

class PeriodTest {

    @Test
    void 시작일은_종료일보다_늦을_수_없다() {
        assertThatThrownBy(() -> new Period(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 2, 1)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 종료일이_없으면_진행_중으로_본다() {
        Period period = new Period(LocalDate.of(2026, 1, 1), null);
        assertThat(period.contains(LocalDate.of(2099, 1, 1))).isTrue();
        assertThat(period.overlaps(YearMonth.of(2025, 12))).isFalse();
        assertThat(period.overlaps(YearMonth.of(2026, 1))).isTrue();
    }

}
