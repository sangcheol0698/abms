package kr.co.abacus.abms.application.payroll.provided;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.application.employee.required.EmployeeRepository;
import kr.co.abacus.abms.application.payroll.required.PayrollRepository;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeFixture;
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
        Employee employee = EmployeeFixture.createEmployee();
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
        Employee employee = EmployeeFixture.createEmployee();
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

}