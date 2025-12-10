package kr.co.abacus.abms.adapter.web.payroll;

import static kr.co.abacus.abms.domain.employee.EmployeeFixture.*;
import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import kr.co.abacus.abms.adapter.web.payroll.dto.PayrollCreateRequest;
import kr.co.abacus.abms.application.employee.required.EmployeeRepository;
import kr.co.abacus.abms.application.payroll.required.PayrollRepository;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.payroll.Payroll;
import kr.co.abacus.abms.domain.shared.Money;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

class PayrollApiTest extends ApiIntegrationTestBase {

    private Employee employee;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PayrollRepository payrollRepository;

    @BeforeEach
    void setUp() {
        employee = createEmployee();

        employeeRepository.save(employee);
    }

    @Test
    void changeSalary() {
        PayrollCreateRequest request = createPayrollCreateRequest(employee.getId());
        String responseJson = objectMapper.writeValueAsString(request);

        restTestClient.post()
            .uri("/api/salary-history")
            .contentType(MediaType.APPLICATION_JSON)
            .body(responseJson)
            .exchange()
            .expectStatus().isNoContent();

        Payroll currentPayroll = payrollRepository.findCurrentSalaryByEmployeeId(employee.getId()).orElseThrow();

        assertThat(currentPayroll.getEmployeeId()).isEqualTo(employee.getId());
        assertThat(currentPayroll.getAnnualSalary()).isEqualTo(Money.wons(request.annualSalary()));
        assertThat(currentPayroll.getPeriod().startDate()).isEqualTo(request.startDate());
        assertThat(currentPayroll.getPeriod().endDate()).isNull();
    }

    private PayrollCreateRequest createPayrollCreateRequest(UUID id) {
        return new PayrollCreateRequest(id, BigDecimal.valueOf(30_000_000), LocalDate.of(2025, 1, 1));
    }

}