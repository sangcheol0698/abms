package kr.co.abacus.abms.application.salaryhistory.provided;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.application.employee.required.EmployeeRepository;
import kr.co.abacus.abms.application.salaryhistory.required.SalaryHistoryRepository;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeFixture;
import kr.co.abacus.abms.domain.salaryhistory.SalaryHistory;
import kr.co.abacus.abms.domain.shared.Money;
import kr.co.abacus.abms.domain.shared.Period;
import kr.co.abacus.abms.support.IntegrationTestBase;

class SalaryHistoryManagerTest extends IntegrationTestBase {

    @Autowired
    private SalaryHistoryManager salaryHistoryManager;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SalaryHistoryRepository salaryHistoryRepository;

    @Test
    @DisplayName("첫 급여 등록")
    void changeSalary() {
        Employee employee = EmployeeFixture.createEmployee();
        employeeRepository.save(employee);

        salaryHistoryManager.changeSalary(employee.getId(), Money.wons(30_000_000L), LocalDate.of(2025, 1, 1));
        flushAndClear();
        SalaryHistory salaryHistory = salaryHistoryRepository.findCurrentSalaryByEmployeeId(employee.getId()).orElseThrow();

        assertThat(salaryHistory.getEmployeeId()).isEqualTo(employee.getId());
        assertThat(salaryHistory.getAnnualSalary()).isEqualTo(Money.wons(30_000_000L));
        assertThat(salaryHistory.getPeriod()).isEqualTo(new Period(LocalDate.of(2025, 1, 1), null));
    }

    @Test
    @DisplayName("급여 변경")
    void changeSalary_alreadyChanged() {
        Employee employee = EmployeeFixture.createEmployee();
        employeeRepository.save(employee);

        salaryHistoryManager.changeSalary(employee.getId(), Money.wons(30_000_000L), LocalDate.of(2025, 1, 1));
        flushAndClear();
        salaryHistoryManager.changeSalary(employee.getId(), Money.wons(40_000_000L), LocalDate.of(2025, 3, 1));
        flushAndClear();

        SalaryHistory salaryHistory = salaryHistoryRepository.findCurrentSalaryByEmployeeId(employee.getId()).orElseThrow();

        assertThat(salaryHistory.getEmployeeId()).isEqualTo(employee.getId());
        assertThat(salaryHistory.getAnnualSalary()).isEqualTo(Money.wons(40_000_000L));
        assertThat(salaryHistory.getPeriod()).isEqualTo(new Period(LocalDate.of(2025, 3, 1), null));
    }

}