package kr.co.abacus.abms.application.payroll.inbound;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.domain.shared.Money;
import kr.co.abacus.abms.support.IntegrationTestBase;

@DisplayName("급여 조회 (PayrollFinder)")
class PayrollFinderTest extends IntegrationTestBase {

    @Autowired
    private PayrollFinder payrollFinder;

    @Autowired
    private PayrollManager payrollManager;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    @DisplayName("현재 연봉과 이력을 조회한다")
    void findPayrolls() {
        Employee employee = employeeRepository.save(createEmployee());

        payrollManager.changeSalary(employee.getId(), Money.wons(30_000_000L), LocalDate.of(2025, 1, 1));
        payrollManager.changeSalary(employee.getId(), Money.wons(36_000_000L), LocalDate.of(2025, 3, 1));
        flushAndClear();

        var current = payrollFinder.findCurrentPayroll(employee.getId());
        var history = payrollFinder.findPayrollHistory(employee.getId());

        assertThat(current).isNotNull();
        assertThat(current.annualSalary()).isEqualTo(36_000_000L);
        assertThat(current.monthlySalary()).isEqualTo(3_000_000L);
        assertThat(current.startDate()).isEqualTo(LocalDate.of(2025, 3, 1));
        assertThat(current.status()).isEqualTo("CURRENT");

        assertThat(history).hasSize(2);
        assertThat(history.getFirst().annualSalary()).isEqualTo(36_000_000L);
        assertThat(history.getFirst().status()).isEqualTo("CURRENT");
        assertThat(history.getFirst().endDate()).isNull();
        assertThat(history.get(1).annualSalary()).isEqualTo(30_000_000L);
        assertThat(history.get(1).status()).isEqualTo("ENDED");
        assertThat(history.get(1).endDate()).isEqualTo(LocalDate.of(2025, 2, 28));
    }

    @Test
    @DisplayName("미래 시작 연봉은 현재 연봉으로 보지 않고 예정 상태로 조회한다")
    void findPayrolls_withScheduledSalary() {
        Employee employee = employeeRepository.save(createEmployee());
        LocalDate today = LocalDate.now();

        payrollManager.changeSalary(employee.getId(), Money.wons(300_000_000L), today.minusMonths(2));
        payrollManager.changeSalary(employee.getId(), Money.wons(350_000_000L), today.plusDays(1));
        flushAndClear();

        var current = payrollFinder.findCurrentPayroll(employee.getId());
        var history = payrollFinder.findPayrollHistory(employee.getId());

        assertThat(current).isNotNull();
        assertThat(current.annualSalary()).isEqualTo(300_000_000L);
        assertThat(current.startDate()).isEqualTo(today.minusMonths(2));

        assertThat(history).hasSize(2);
        assertThat(history.getFirst().annualSalary()).isEqualTo(350_000_000L);
        assertThat(history.getFirst().status()).isEqualTo("SCHEDULED");
        assertThat(history.get(1).annualSalary()).isEqualTo(300_000_000L);
        assertThat(history.get(1).status()).isEqualTo("CURRENT");
    }

    private Employee createEmployee() {
        return Employee.create(
                1L,
                "홍길동",
                "payroll-finder@email.com",
                LocalDate.of(2020, 1, 1),
                LocalDate.of(1990, 1, 1),
                EmployeePosition.TEAM_LEADER,
                EmployeeType.FULL_TIME,
                EmployeeGrade.SENIOR,
                EmployeeAvatar.SKY_GLOW,
                null);
    }

}
