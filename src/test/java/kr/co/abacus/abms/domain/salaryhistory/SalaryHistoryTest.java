package kr.co.abacus.abms.domain.salaryhistory;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import kr.co.abacus.abms.domain.shared.Money;

class SalaryHistoryTest {

    @Test
    void startWith() {
        SalaryHistory salaryHistory = SalaryHistory.startWith(UUID.randomUUID(), Money.wons(100_00L), LocalDate.of(2025, 1, 1));

        assertThat(salaryHistory.getEmployeeId()).isNotNull();
        assertThat(salaryHistory.getAnnualSalary()).isEqualTo(Money.wons(100_00L));
        assertThat(salaryHistory.getPeriod().startDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(salaryHistory.getPeriod().endDate()).isNull();
    }

    @Test
    void close() {
        SalaryHistory salaryHistory = SalaryHistory.startWith(UUID.randomUUID(), Money.wons(100_00L), LocalDate.of(2025, 1, 1));
        salaryHistory.close(LocalDate.of(2025, 12, 31));

        assertThat(salaryHistory.getPeriod().endDate()).isEqualTo(LocalDate.of(2025, 12, 31));
    }

}