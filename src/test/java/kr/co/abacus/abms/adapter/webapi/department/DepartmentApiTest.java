package kr.co.abacus.abms.adapter.webapi.department;

import static org.assertj.core.api.Assertions.*;

import java.io.UnsupportedEncodingException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.co.abacus.abms.adapter.webapi.department.dto.OrganizationChartResponse;
import kr.co.abacus.abms.adapter.webapi.department.dto.OrganizationChartWithEmployeesResponse;
import kr.co.abacus.abms.adapter.webapi.department.dto.OrganizationEmployeeResponse;
import kr.co.abacus.abms.application.department.required.DepartmentRepository;
import kr.co.abacus.abms.application.employee.required.EmployeeRepository;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentFixture;
import kr.co.abacus.abms.domain.department.DepartmentType;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeFixture;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;


class DepartmentApiTest extends ApiIntegrationTestBase {

    @Autowired
    private DepartmentRepository departmentRepository;

    private Department company;
    private Department division;
    private Department team1;
    private Department team2;

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUpDepartments() {
        Employee employee = EmployeeFixture.createEmployee();
        employeeRepository.save(employee);
        flushAndClear();

        company = Department.createRoot(
            DepartmentFixture.createDepartmentCreateRequest("테스트회사", "TEST_COM", DepartmentType.COMPANY, employee.getId())
        );
        departmentRepository.save(company);
        division = Department.create(
            DepartmentFixture.createDepartmentCreateRequest("테스트본부", "TEST_DIV", DepartmentType.DIVISION, null),
            company
        );
        departmentRepository.save(division);
        team1 = Department.create(
            DepartmentFixture.createDepartmentCreateRequest("테스트팀", "TEST_TEAM", DepartmentType.TEAM, null),
            division
        );
        team2 = Department.create(
            DepartmentFixture.createDepartmentCreateRequest("테스트팀2", "TEST_TEAM2", DepartmentType.TEAM, null),
            division
        );
        departmentRepository.save(team1);
        departmentRepository.save(team2);
        flushAndClear();

        employeeRepository.save(Employee.create(EmployeeFixture.createEmployeeCreateRequest("team2@email.com", "김철수", team1.getId())));
        flushAndClear();
    }

    @Test
    @DisplayName("전체 부서 계층 구조를 올바르게 반환한다")
    void getOrganizationChart() throws Exception {
        OrganizationChartResponse response = restTestClient.get()
            .uri("/api/departments/organization-chart")
            .exchange()
            .expectStatus().isOk()
            .expectBody(OrganizationChartResponse.class)
            .returnResult()
            .getResponseBody();

        assertDepartmentNode(response, company, 1);
        assertThat(response.departmentLeader().employeeName()).isEqualTo("홍길동");

        OrganizationChartResponse divisionNode = response.children().getFirst();
        assertDepartmentNode(divisionNode, division, 2);

        assertThat(divisionNode.children())
            .extracting(OrganizationChartResponse::departmentName)
            .containsExactlyInAnyOrder(team1.getName(), team2.getName());
    }

    @Test
    @DisplayName("전체 부서와 직원을 같이 조회한다")
    void getOrganizationChartWithEmployees() throws Exception {
        OrganizationChartWithEmployeesResponse response = restTestClient.get().uri("/api/departments/organization-chart/employees")
            .exchange()
            .expectStatus().isOk()
            .expectBody(OrganizationChartWithEmployeesResponse.class)
            .returnResult()
            .getResponseBody();

        assertDepartmentNode(response, company, 1);
        Assertions.assertNotNull(response.departmentLeader());
        assertThat(response.departmentLeader().employeeName()).isEqualTo("홍길동");

        OrganizationChartWithEmployeesResponse divisionNode = response.children().getFirst();
        assertDepartmentNode(divisionNode, division, 2);

        assertThat(divisionNode.children())
            .extracting(OrganizationChartWithEmployeesResponse::departmentName)
            .containsExactlyInAnyOrder(team1.getName(), team2.getName());

        OrganizationChartWithEmployeesResponse team1Response = response.children().getFirst().children().stream()
            .filter(node -> node.departmentId().equals(team1.getId()))
            .findFirst()
            .orElseThrow();
        assertDepartmentNode(team1Response, team1, 0);

        assertThat(team1Response.employees()).hasSize(1)
            .extracting(OrganizationEmployeeResponse::employeeName)
            .containsExactly("김철수");
    }

    @Test
    void getDepartment() throws UnsupportedEncodingException, JsonProcessingException {
        DepartmentResponse response = restTestClient.get().uri("/api/departments/{id}", team1.getId())
            .exchange()
            .expectStatus().isOk()
            .expectBody(DepartmentResponse.class)
            .returnResult()
            .getResponseBody();

        assertThat(response.departmentId()).isEqualTo(team1.getId());
        assertThat(response.departmentName()).isEqualTo(team1.getName());
        assertThat(response.departmentCode()).isEqualTo(team1.getCode());
        assertThat(response.departmentType()).isEqualTo(team1.getType().getDescription());
    }

    private void assertDepartmentNode(OrganizationChartResponse node, Department expected, int expectedChildrenSize) {
        assertThat(node.departmentId()).isEqualTo(expected.getId());
        assertThat(node.departmentName()).isEqualTo(expected.getName());
        assertThat(node.departmentCode()).isEqualTo(expected.getCode());
        assertThat(node.departmentType()).isEqualTo(expected.getType().getDescription()); // Enum -> String 변환 로직에 맞게 수정
        assertThat(node.children()).hasSize(expectedChildrenSize);
    }

    private void assertDepartmentNode(OrganizationChartWithEmployeesResponse node, Department expected, int expectedChildrenSize) {
        assertThat(node.departmentId()).isEqualTo(expected.getId());
        assertThat(node.departmentName()).isEqualTo(expected.getName());
        assertThat(node.departmentCode()).isEqualTo(expected.getCode());
        assertThat(node.departmentType()).isEqualTo(expected.getType().getDescription()); // Enum -> String 변환 로직에 맞게 수정
        assertThat(node.children()).hasSize(expectedChildrenSize);
    }

}