package kr.co.abacus.abms.adapter.webapi.employee;

import static kr.co.abacus.abms.domain.employee.EmployeeFixture.*;
import static kr.co.abacus.abms.support.AssertThatUtils.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeeCreateResponse;
import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeeGradeResponse;
import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeePositionResponse;
import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeeResponse;
import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeeStatusResponse;
import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeeTypeResponse;
import kr.co.abacus.abms.application.department.required.DepartmentRepository;
import kr.co.abacus.abms.application.employee.provided.EmployeeManager;
import kr.co.abacus.abms.application.employee.required.EmployeeRepository;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentFixture;
import kr.co.abacus.abms.domain.department.DepartmentType;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeCreateRequest;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeStatus;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

class EmployeeApiTest extends ApiIntegrationTestBase {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeManager employeeManager;

    @Autowired
    private DepartmentRepository departmentRepository;

    private UUID companyId;
    private UUID divisionId;
    private UUID teamId;

    @BeforeEach
    void setUpDepartments() {
        Department company = DepartmentFixture.createTestCompany();
        departmentRepository.save(company);
        Department division = Department.create(
            DepartmentFixture.createDepartmentCreateRequest("테스트본부", "TEST_DIV", DepartmentType.DIVISION, null),
            company
        );
        departmentRepository.save(division);
        Department team = Department.create(
            DepartmentFixture.createDepartmentCreateRequest("테스트팀", "TEST_TEAM", DepartmentType.TEAM, null),
            division
        );
        departmentRepository.save(team);
        flushAndClear();
        companyId = company.getId();
        divisionId = division.getId();
        teamId = team.getId();
    }

    @Test
    void create() throws JsonProcessingException, UnsupportedEncodingException {
        EmployeeCreateRequest request = createEmployeeCreateRequestWithDepartment(companyId);
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
        EmployeeCreateRequest request = createEmployeeCreateRequestWithDepartment(companyId, "invalid-email");
        String responseJson = objectMapper.writeValueAsString(request);

        assertThat(mvcTester.post().uri("/api/employees").contentType(MediaType.APPLICATION_JSON)
            .content(responseJson))
            .apply(print())
            .hasStatus(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void create_duplicateEmail() throws JsonProcessingException {
        employeeManager.create(createEmployeeCreateRequestWithDepartment(companyId));

        EmployeeCreateRequest request = createEmployeeCreateRequestWithDepartment(companyId);
        String responseJson = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.post().uri("/api/employees").contentType(MediaType.APPLICATION_JSON)
            .content(responseJson).exchange();

        assertThat(result)
            .apply(print())
            .hasStatus(HttpStatus.CONFLICT.value());
    }

    @Test
    void find() throws UnsupportedEncodingException, JsonProcessingException {
        Employee savedEmployee = employeeManager.create(createEmployeeCreateRequestWithDepartment(companyId));

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

    @Test
    void getEmployeeGrades() throws Exception {
        MvcTestResult result = mvcTester.get().uri("/api/employees/grades").exchange();

        assertThat(result).apply(print()).hasStatusOk();

        List<EmployeeGradeResponse> responses = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            new TypeReference<>() {}
        );

        assertThat(responses).hasSize(EmployeeGrade.values().length);

        for (EmployeeGrade grade : EmployeeGrade.values()) {
            EmployeeGradeResponse found = responses.stream()
                .filter(r -> r.name().equals(grade.name()))
                .findFirst()
                .orElseThrow();

            assertThat(found.name()).isEqualTo(grade.name());
            assertThat(found.description()).isEqualTo(grade.getDescription());
            assertThat(found.level()).isEqualTo(grade.getLevel());
        }
    }

    @Test
    void getEmployeePositions() throws Exception {
        MvcTestResult result = mvcTester.get().uri("/api/employees/positions").exchange();

        assertThat(result).apply(print()).hasStatusOk();

        List<EmployeePositionResponse> responses = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            new TypeReference<>() {
            }
        );

        assertThat(responses).hasSize(EmployeePosition.values().length);

        for (EmployeePosition position : EmployeePosition.values()) {
            EmployeePositionResponse found = responses.stream()
                .filter(r -> r.name().equals(position.name()))
                .findFirst()
                .orElseThrow();

            assertThat(found.name()).isEqualTo(position.name());
            assertThat(found.description()).isEqualTo(position.getDescription());
            assertThat(found.rank()).isEqualTo(position.getRank());
        }
    }

    @Test
    void getEmployeeTypes() throws Exception {
        MvcTestResult result = mvcTester.get().uri("/api/employees/types").exchange();

        assertThat(result).apply(print()).hasStatusOk();

        List<EmployeeTypeResponse> responses = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            new TypeReference<>() {
            }
        );

        assertThat(responses).hasSize(EmployeeType.values().length);

        for (EmployeeType type : EmployeeType.values()) {
            EmployeeTypeResponse found = responses.stream()
                .filter(r -> r.name().equals(type.name()))
                .findFirst()
                .orElseThrow();

            assertThat(found.name()).isEqualTo(type.name());
            assertThat(found.description()).isEqualTo(type.getDescription());
        }
    }

    @Test
    void getEmployeeStatuses() throws Exception {
        MvcTestResult result = mvcTester.get().uri("/api/employees/statuses").exchange();

        assertThat(result).apply(print()).hasStatusOk();

        List<EmployeeStatusResponse> responses = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            new TypeReference<>() {
            }
        );

        assertThat(responses).hasSize(EmployeeStatus.values().length);

        for (EmployeeStatus status : EmployeeStatus.values()) {
            EmployeeStatusResponse found = responses.stream()
                .filter(r -> r.name().equals(status.name()))
                .findFirst()
                .orElseThrow();

            assertThat(found.name()).isEqualTo(status.name());
            assertThat(found.description()).isEqualTo(status.getDescription());
        }
    }

    @Test
    void update() throws Exception {
        Employee employee = employeeManager.create(createEmployeeCreateRequestWithDepartment(teamId, "update-target@email.com"));
        flushAndClear();

        var request = createEmployeeUpdateRequestWithDepartment(divisionId, "김수정", "updated@email.com");
        String responseJson = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.put()
            .uri("/api/employees/{id}", employee.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(responseJson)
            .exchange();
        flushAndClear();

        assertThat(result)
            .apply(print())
            .hasStatusOk()
            .bodyJson()
            .hasPathSatisfying("$.departmentId", equalsTo(divisionId.toString()))
            .hasPathSatisfying("$.name", equalsTo(request.name()))
            .hasPathSatisfying("$.email", equalsTo(request.email()))
            .hasPathSatisfying("$.joinDate", equalsTo(request.joinDate().toString()))
            .hasPathSatisfying("$.birthDate", equalsTo(request.birthDate().toString()))
            .hasPathSatisfying("$.memo", equalsTo(request.memo()));

        Employee updatedEmployee = employeeRepository.findById(employee.getId()).orElseThrow();
        assertThat(updatedEmployee.getDepartmentId()).isEqualTo(request.departmentId());
        assertThat(updatedEmployee.getName()).isEqualTo(request.name());
        assertThat(updatedEmployee.getEmail().address()).isEqualTo(request.email());
        assertThat(updatedEmployee.getJoinDate()).isEqualTo(request.joinDate());
        assertThat(updatedEmployee.getBirthDate()).isEqualTo(request.birthDate());
        assertThat(updatedEmployee.getPosition()).isEqualTo(request.position());
        assertThat(updatedEmployee.getType()).isEqualTo(request.type());
        assertThat(updatedEmployee.getGrade()).isEqualTo(request.grade());
        assertThat(updatedEmployee.getMemo()).isEqualTo(request.memo());
    }

    @Test
    void update_duplicateEmail() throws Exception {
        Employee employee1 = employeeManager.create(createEmployeeCreateRequestWithDepartment(teamId, "dup1@email.com"));
        Employee employee2 = employeeManager.create(createEmployeeCreateRequestWithDepartment(teamId, "dup2@email.com"));
        flushAndClear();

        var request = createEmployeeUpdateRequestWithDepartment(divisionId, employee1.getName(), employee2.getEmail().address());
        String responseJson = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.put()
            .uri("/api/employees/{id}", employee1.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(responseJson)
            .exchange();

        assertThat(result)
            .apply(print())
            .hasStatus(HttpStatus.CONFLICT.value());
    }

}
