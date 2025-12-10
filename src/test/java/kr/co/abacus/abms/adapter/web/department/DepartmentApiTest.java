package kr.co.abacus.abms.adapter.web.department;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;

import kr.co.abacus.abms.adapter.web.PageResponse;
import kr.co.abacus.abms.adapter.web.department.dto.EmployeeAssignTeamLeaderRequest;
import kr.co.abacus.abms.adapter.web.department.dto.OrganizationChartResponse;
import kr.co.abacus.abms.adapter.web.department.dto.OrganizationChartWithEmployeesResponse;
import kr.co.abacus.abms.adapter.web.department.dto.OrganizationEmployeeResponse;
import kr.co.abacus.abms.application.department.required.DepartmentRepository;
import kr.co.abacus.abms.application.employee.dto.EmployeeResponse;
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
    private Employee employee1;

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

        employee1 = employeeRepository.save(Employee.create(EmployeeFixture.createEmployeeCreateRequest("team2@email.com", "김철수", team1.getId())));
        flushAndClear();
    }

    @Test
    @DisplayName("전체 부서 계층 구조를 올바르게 반환한다")
    void getOrganizationChart() {
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
    void getOrganizationChartWithEmployees() {
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
    @DisplayName("부서 상세 정보를 조회한다")
    void getDepartment() {
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

    @Test
    @DisplayName("부서 소속 직원들을 페이징하여 조회한다")
    void getDepartmentEmployees_withPaging() throws Exception {
        // Given: 부서에 여러 직원 생성
        for (int i = 1; i <= 15; i++) {
            Employee employee = Employee.create(
                EmployeeFixture.createEmployeeCreateRequest("emp" + i + "@test.com", "직원" + i, team1.getId())
            );
            employeeRepository.save(employee);
        }
        flushAndClear();

        // When: 첫 번째 페이지 조회 (size=10)
        PageResponse<EmployeeResponse> response = restTestClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/api/departments/{departmentId}/employees")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .build(team1.getId()))
            .exchange()
            .expectStatus().isOk()
            .expectBody(new ParameterizedTypeReference<PageResponse<EmployeeResponse>>() {
            })
            .returnResult()
            .getResponseBody();

        // Then: 페이징 정보 검증
        assertThat(response.content()).hasSize(10);
        assertThat(response.totalElements()).isEqualTo(16); // 김철수 + 15명
        assertThat(response.totalPages()).isEqualTo(2);
        assertThat(response.pageNumber()).isEqualTo(0);
    }

    @Test
    @DisplayName("부서 소속 직원이 없을 때 빈 페이지를 반환한다")
    void getDepartmentEmployees_emptyDepartment() throws Exception {
        // Given: 직원이 없는 부서 (team2)
        
        // When: 직원 조회
        PageResponse<EmployeeResponse> response = restTestClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/api/departments/{departmentId}/employees")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .build(team2.getId()))
            .exchange()
            .expectStatus().isOk()
            .expectBody(new ParameterizedTypeReference<PageResponse<EmployeeResponse>>() {
            })
            .returnResult()
            .getResponseBody();

        // Then: 빈 결과 반환
        assertThat(response.content()).isEmpty();
        assertThat(response.totalElements()).isEqualTo(0);
        assertThat(response.totalPages()).isEqualTo(0);
    }

    @Test
    @DisplayName("존재하지 않는 부서 ID로 직원 조회 시 404를 반환한다")
    void getDepartmentEmployees_notFoundDepartment() throws Exception {
        // Given: 존재하지 않는 부서 ID
        UUID nonExistentId = UUID.randomUUID();

        // When & Then: 404 응답
        restTestClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/api/departments/{departmentId}/employees")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .build(nonExistentId))
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("팀의 리더를 임명한다")
    void assignTeamLeader() {
        restTestClient.post()
            .uri("/api/departments/{departmentId}/assign-team-leader", team1.getId())
            .body(new EmployeeAssignTeamLeaderRequest(employee1.getId()))
            .exchange()
            .expectStatus().isOk()
            .returnResult();
        flushAndClear();

        Department department = departmentRepository.findByIdAndDeletedFalse(team1.getId()).orElseThrow();

        assertThat(department.getLeaderEmployeeId()).isEqualTo(employee1.getId());
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