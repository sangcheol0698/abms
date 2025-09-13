package kr.co.abacus.abms.adapter.webapi.employee;

import static kr.co.abacus.abms.domain.employee.EmployeeFixture.*;
import static kr.co.abacus.abms.support.AssertThatUtils.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import java.io.UnsupportedEncodingException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeeCreateResponse;
import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeeResponse;
import kr.co.abacus.abms.application.employee.provided.EmployeeManager;
import kr.co.abacus.abms.application.employee.required.EmployeeRepository;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeCreateRequest;
import kr.co.abacus.abms.domain.employee.EmployeeStatus;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

class EmployeeApiTest extends ApiIntegrationTestBase {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeManager employeeManager;

    @Test
    void create() throws JsonProcessingException, UnsupportedEncodingException {
        EmployeeCreateRequest request = createEmployeeCreateRequestWithDepartment(getDefaultDepartmentId());
        String responseJson = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.post().uri("/api/employees").contentType(MediaType.APPLICATION_JSON)
            .content(responseJson).exchange();
        flushAndClear();

        assertThat(result)
            .apply(print())
            .hasStatusOk()
            .bodyJson()
            .hasPathSatisfying("$.employeeId", notNull())
            .hasPathSatisfying("$.email", equalsTo(request.email()));

        EmployeeCreateResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), EmployeeCreateResponse.class);
        Employee employee = employeeRepository.findById(response.employeeId()).orElseThrow();

        assertThat(employee.getName()).isEqualTo(request.name());
        assertThat(employee.getEmail().address()).isEqualTo(request.email());
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
    }

    @Test
    void create_invalidEmail() throws JsonProcessingException {
        EmployeeCreateRequest request = createEmployeeCreateRequestWithDepartment(getDefaultDepartmentId(), "invalid-email");
        String responseJson = objectMapper.writeValueAsString(request);

        assertThat(mvcTester.post().uri("/api/employees").contentType(MediaType.APPLICATION_JSON)
            .content(responseJson))
            .apply(print())
            .hasStatus(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void create_duplicateEmail() throws JsonProcessingException {
        employeeManager.create(createEmployeeCreateRequestWithDepartment(getDefaultDepartmentId()));

        EmployeeCreateRequest request = createEmployeeCreateRequestWithDepartment(getDefaultDepartmentId());
        String responseJson = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.post().uri("/api/employees").contentType(MediaType.APPLICATION_JSON)
            .content(responseJson).exchange();

        assertThat(result)
            .apply(print())
            .hasStatus(HttpStatus.CONFLICT.value());
    }

    @Test
    void find() throws UnsupportedEncodingException, JsonProcessingException {
        Employee savedEmployee = employeeManager.create(createEmployeeCreateRequestWithDepartment(getDefaultDepartmentId()));

        MvcTestResult result = mvcTester.get().uri("/api/employees/{id}", savedEmployee.getId()).exchange();

        assertThat(result)
            .apply(print())
            .hasStatusOk()
            .bodyJson()
            .hasPathSatisfying("$.departmentId", equalsTo(savedEmployee.getDepartmentId().toString()))
            .hasPathSatisfying("$.departmentName", equalsTo("테스트회사"))
            .hasPathSatisfying("$.employeeId", equalsTo(savedEmployee.getId().toString()))
            .hasPathSatisfying("$.name", equalsTo(savedEmployee.getName()))
            .hasPathSatisfying("$.email", equalsTo(savedEmployee.getEmail().address()));

        EmployeeResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);

        assertThat(response.departmentId()).isEqualTo(savedEmployee.getDepartmentId());
        assertThat(response.departmentName()).isEqualTo("테스트회사");
        assertThat(response.employeeId()).isEqualTo(savedEmployee.getId().toString());
        assertThat(response.name()).isEqualTo(savedEmployee.getName());
        assertThat(response.email()).isEqualTo(savedEmployee.getEmail().address());
    }

}