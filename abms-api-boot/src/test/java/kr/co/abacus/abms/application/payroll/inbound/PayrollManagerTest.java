package kr.co.abacus.abms.application.payroll.inbound;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.payroll.outbound.PayrollRepository;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.domain.payroll.Payroll;
import kr.co.abacus.abms.domain.shared.Money;
import kr.co.abacus.abms.domain.shared.Period;
import kr.co.abacus.abms.support.IntegrationTestBase;

@DisplayName("급여 관리 (PayrollManager)")
class PayrollManagerTest extends IntegrationTestBase {

    @Autowired
    private PayrollManager payrollManager;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PayrollRepository payrollRepository;

    @Test
    @DisplayName("직원의 연봉을 변경한다 (첫 등록 포함)")
    void changeSalary() {
        Employee employee = createEmployee();
        employeeRepository.save(employee);

        payrollManager.changeSalary(employee.getId(), Money.wons(30_000_000L), LocalDate.of(2025, 1, 1));
        flushAndClear();
        Payroll payroll = payrollRepository.findCurrentSalaryByEmployeeId(employee.getId()).orElseThrow();

        assertThat(payroll.getEmployeeId()).isEqualTo(employee.getId());
        assertThat(payroll.getAnnualSalary()).isEqualTo(Money.wons(30_000_000L));
        assertThat(payroll.getPeriod()).isEqualTo(new Period(LocalDate.of(2025, 1, 1), null));
    }

    @Test
    @DisplayName("이미 등록된 연봉을 새로운 기간으로 변경한다")
    void changeSalary_alreadyChanged() {
        Employee employee = createEmployee();
        employeeRepository.save(employee);

        payrollManager.changeSalary(employee.getId(), Money.wons(30_000_000L), LocalDate.of(2025, 1, 1));
        flushAndClear();
        payrollManager.changeSalary(employee.getId(), Money.wons(40_000_000L), LocalDate.of(2025, 3, 1));
        flushAndClear();

        Payroll payroll = payrollRepository.findCurrentSalaryByEmployeeId(employee.getId()).orElseThrow();

        assertThat(payroll.getEmployeeId()).isEqualTo(employee.getId());
        assertThat(payroll.getAnnualSalary()).isEqualTo(Money.wons(40_000_000L));
        assertThat(payroll.getPeriod()).isEqualTo(new Period(LocalDate.of(2025, 3, 1), null));
    }

    @Test
    @DisplayName("현재 연봉 시작일과 같거나 이전 날짜로는 변경할 수 없다")
    void changeSalary_fail_whenStartDateIsNotAfterCurrentSalaryStartDate() {
        Employee employee = createEmployee();
        employeeRepository.save(employee);

        payrollManager.changeSalary(employee.getId(), Money.wons(30_000_000L), LocalDate.of(2025, 3, 1));
        flushAndClear();

        assertThatThrownBy(() ->
                payrollManager.changeSalary(employee.getId(), Money.wons(40_000_000L), LocalDate.of(2025, 3, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("새 연봉 적용 시작일은 기존 연봉 시작일 이후여야 합니다.");

        assertThatThrownBy(() ->
                payrollManager.changeSalary(employee.getId(), Money.wons(40_000_000L), LocalDate.of(2025, 2, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("새 연봉 적용 시작일은 기존 연봉 시작일 이후여야 합니다.");
    }

    @Test
    @DisplayName("종료일이 있는 기존 연봉과 겹치면 새 시작일 전날로 종료일을 조정한다")
    void changeSalary_closesOverlappingFixedRangePayroll() {
        Employee employee = createEmployee();
        employeeRepository.save(employee);

        Payroll payroll = Payroll.create(employee.getId(), Money.wons(300_000_000L), LocalDate.of(2026, 1, 1));
        payroll.close(LocalDate.of(2026, 12, 31));
        payrollRepository.save(payroll);
        flushAndClear();

        payrollManager.changeSalary(employee.getId(), Money.wons(310_000_000L), LocalDate.of(2026, 3, 19));
        flushAndClear();

        var history = payrollRepository.findAllByEmployeeId(employee.getId());

        assertThat(history).hasSize(2);
        Payroll currentPayroll = history.stream()
                .filter(candidate -> candidate.getAnnualSalary().equals(Money.wons(310_000_000L)))
                .findFirst()
                .orElseThrow();
        Payroll previousPayroll = history.stream()
                .filter(candidate -> candidate.getAnnualSalary().equals(Money.wons(300_000_000L)))
                .findFirst()
                .orElseThrow();

        assertThat(currentPayroll.getPeriod()).isEqualTo(new Period(LocalDate.of(2026, 3, 19), null));
        assertThat(previousPayroll.getPeriod()).isEqualTo(new Period(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 18)));
    }

    private Employee createEmployee() {
        return Employee.create(
                1L,
                "홍길동",
                "test@email.com",
                LocalDate.of(2020, 1, 1),
                LocalDate.of(1990, 1, 1),
                EmployeePosition.TEAM_LEADER,
                EmployeeType.FULL_TIME,
                EmployeeGrade.SENIOR,
                EmployeeAvatar.SKY_GLOW,
                "This is a memo for the employee.");
    }

}
