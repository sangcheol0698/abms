package kr.co.abacus.abms.project;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.YearMonth;

import org.junit.jupiter.api.Test;

import kr.co.abacus.abms.common.domain.Money;
import kr.co.abacus.abms.common.domain.Period;

class ProjectManagedRevenueTest {

    @Test
    void 월별_관리_매출을_모두_더하면_계약금액과_같다() {
        Project project = project(1_000_000_001, LocalDate.of(2026, 1, 15), LocalDate.of(2026, 7, 10));

        Money sum = Money.ZERO;
        for (YearMonth m = YearMonth.of(2025, 12); !m.isAfter(YearMonth.of(2026, 8)); m = m.plusMonths(1)) {
            sum = sum.plus(project.managedRevenue(m));
        }

        assertThat(sum).isEqualTo(Money.wons(1_000_000_001));
    }

    @Test
    void 프로젝트_기간_밖의_월은_관리_매출이_없다() {
        Project project = project(300_000_000, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31));

        assertThat(project.managedRevenue(YearMonth.of(2025, 12))).isEqualTo(Money.ZERO);
        assertThat(project.managedRevenue(YearMonth.of(2026, 4))).isEqualTo(Money.ZERO);
    }

    @Test
    void 일수_비율로_배분한다() {
        // 90일 중 1월 31일, 2월 28일, 3월 31일
        Project project = project(90_000_000, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31));

        assertThat(project.managedRevenue(YearMonth.of(2026, 1))).isEqualTo(Money.wons(31_000_000));
        assertThat(project.managedRevenue(YearMonth.of(2026, 2))).isEqualTo(Money.wons(28_000_000));
        assertThat(project.managedRevenue(YearMonth.of(2026, 3))).isEqualTo(Money.wons(31_000_000));
    }

    private static Project project(long contractAmount, LocalDate start, LocalDate end) {
        return Project.create("P-1", new Project.ProjectInfo(1L, 1L, "프로젝트", null, ProjectStatus.IN_PROGRESS,
                Money.wons(contractAmount), new Period(start, end)));
    }

}
