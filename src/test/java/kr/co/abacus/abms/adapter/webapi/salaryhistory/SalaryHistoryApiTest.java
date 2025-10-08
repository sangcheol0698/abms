package kr.co.abacus.abms.adapter.webapi.salaryhistory;

import static kr.co.abacus.abms.domain.employee.EmployeeFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.co.abacus.abms.adapter.webapi.salaryhistory.dto.SalaryHistoryCreateRequest;
import kr.co.abacus.abms.application.employee.required.EmployeeRepository;
import kr.co.abacus.abms.application.salaryhistory.required.SalaryHistoryRepository;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.salaryhistory.SalaryHistory;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

class SalaryHistoryApiTest extends ApiIntegrationTestBase {

    private Employee employee;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SalaryHistoryRepository salaryHistoryRepository;

    @BeforeEach
    void setUp() {
        employee = createEmployee();

        employeeRepository.save(employee);
    }

    @Test
    void changeSalary() throws JsonProcessingException {
        SalaryHistoryCreateRequest request = createSalaryHistoryCreateRequest(employee.getId());
        String responseJson = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.post().uri("/api/salary-history").contentType(MediaType.APPLICATION_JSON)
            .content(responseJson)
            .exchange();

        assertThat(result).apply(print()).hasStatus(HttpStatus.NO_CONTENT);

        SalaryHistory currentSalaryHistory = salaryHistoryRepository.findCurrentSalaryByEmployeeId(employee.getId()).orElseThrow();

        assertThat(currentSalaryHistory.getEmployeeId()).isEqualTo(employee.getId());
        assertThat(currentSalaryHistory.getAnnualSalary().amount()).isEqualTo(request.annualSalary());
        assertThat(currentSalaryHistory.getPeriod().startDate()).isEqualTo(request.startDate());
        assertThat(currentSalaryHistory.getPeriod().endDate()).isNull();
    }

    private SalaryHistoryCreateRequest createSalaryHistoryCreateRequest(UUID id) {
        return new SalaryHistoryCreateRequest(id, BigDecimal.valueOf(30_000_000), LocalDate.of(2025, 1, 1));
    }

}