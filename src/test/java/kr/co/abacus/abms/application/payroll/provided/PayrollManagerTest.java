package kr.co.abacus.abms.application.payroll.provided;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.payroll.required.PayrollRepository;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.domain.payroll.Payroll;
import kr.co.abacus.abms.domain.shared.Money;
import kr.co.abacus.abms.domain.shared.Period;
import kr.co.abacus.abms.support.IntegrationTestBase;

class PayrollManagerTest extends IntegrationTestBase {

    @Autowired
    private PayrollManager payrollManager;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PayrollRepository payrollRepository;

    @Test
    @DisplayName("첫 급여 등록")
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
    @DisplayName("급여 변경")
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

    private Employee createEmployee() {
        return Employee.create(
            UUID.randomUUID(),
            "홍길동",
            "test@email.com",
            LocalDate.of(2020, 1, 1),
            LocalDate.of(1990, 1, 1),
            EmployeePosition.MANAGER,
            EmployeeType.FULL_TIME,
            EmployeeGrade.SENIOR,
            EmployeeAvatar.SKY_GLOW,
            "This is a memo for the employee."
        );
    }

}