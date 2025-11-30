package kr.co.abacus.abms.domain.payroll;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import kr.co.abacus.abms.domain.shared.Money;

class PayrollTest {

    @Test
    void startWith() {
        Payroll payroll = Payroll.startWith(UUID.randomUUID(), Money.wons(100_00L), LocalDate.of(2025, 1, 1));

        assertThat(payroll.getEmployeeId()).isNotNull();
        assertThat(payroll.getAnnualSalary()).isEqualTo(Money.wons(100_00L));
        assertThat(payroll.getPeriod().startDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(payroll.getPeriod().endDate()).isNull();
    }

    @Test
    void close() {
        Payroll payroll = Payroll.startWith(UUID.randomUUID(), Money.wons(100_00L), LocalDate.of(2025, 1, 1));
        payroll.close(LocalDate.of(2025, 12, 31));

        assertThat(payroll.getPeriod().endDate()).isEqualTo(LocalDate.of(2025, 12, 31));
    }

}