package kr.co.abacus.abms.adapter.web.department;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;

import kr.co.abacus.abms.adapter.web.PageResponse;
import kr.co.abacus.abms.adapter.web.department.dto.EmployeeAssignTeamLeaderRequest;
import kr.co.abacus.abms.adapter.web.department.dto.OrganizationChartResponse;
import kr.co.abacus.abms.adapter.web.department.dto.OrganizationChartWithEmployeesResponse;
import kr.co.abacus.abms.adapter.web.department.dto.OrganizationEmployeeResponse;
import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.dto.EmployeeSummary;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentType;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

class DepartmentApiTest extends ApiIntegrationTestBase {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    @DisplayName("전체 부서 계층 구조를 올바르게 반환한다")
    void getOrganizationChart() {
        Department company = createDepartment("COMP001", "ABC Corp", DepartmentType.COMPANY, null, null);
        Department division = createDepartment("DIV001", "ABC Corp", DepartmentType.DIVISION, null, company);
        Department team1 = createDepartment("TEAM001", "ABC Corp", DepartmentType.TEAM, null, division);
        Department team2 = createDepartment("TEAM002", "ABC Corp", DepartmentType.TEAM, null, division);
        departmentRepository.saveAll(List.of(company, division, team1, team2));

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
        Department company = createDepartment("COMP001", "ABC Corp", DepartmentType.COMPANY, null, null);
        Department division = createDepartment("DIV001", "ABC Corp", DepartmentType.DIVISION, null, company);
        Department team1 = createDepartment("TEAM001", "ABC Corp", DepartmentType.TEAM, null, division);
        Department team2 = createDepartment("TEAM002", "ABC Corp", DepartmentType.TEAM, null, division);
        departmentRepository.saveAll(List.of(company, division, team1, team2));

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
        Department company = createDepartment("COMP001", "ABC Corp", DepartmentType.COMPANY, null, null);
        Department division = createDepartment("DIV001", "ABC Corp", DepartmentType.DIVISION, null, company);
        Department team1 = createDepartment("TEAM001", "ABC Corp", DepartmentType.TEAM, null, division);
        departmentRepository.saveAll(List.of(company, division, team1));

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
    void getDepartmentEmployees_withPaging() {
        // Given: 부서에 여러 직원 생성
        Department company = createDepartment("COMP001", "ABC Corp", DepartmentType.COMPANY, null, null);
        Department division = createDepartment("DIV001", "ABC Corp", DepartmentType.DIVISION, null, company);
        Department team1 = createDepartment("TEAM001", "ABC Corp", DepartmentType.TEAM, null, division);

        for (int i = 1; i <= 15; i++) {
            Employee employee = createEmployee("employee" + i + "@email.com");
            employeeRepository.save(employee);
        }
        flushAndClear();

        // When: 첫 번째 페이지 조회 (size=10)
        PageResponse<EmployeeSummary> response = restTestClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/api/departments/{departmentId}/employees")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .build(team1.getId()))
            .exchange()
            .expectStatus().isOk()
            .expectBody(new ParameterizedTypeReference<PageResponse<EmployeeSummary>>() {
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
    @DisplayName("존재하지 않는 부서 ID로 직원 조회 시 404를 반환한다")
    void getDepartmentEmployees_notFoundDepartment() {
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
        Department company = createDepartment("COMP001", "ABC Corp", DepartmentType.COMPANY, null, null);
        Department division = createDepartment("DIV001", "ABC Corp", DepartmentType.DIVISION, null, company);
        Department team1 = createDepartment("TEAM001", "ABC Corp", DepartmentType.TEAM, null, division);
        departmentRepository.saveAll(List.of(company, division, team1));

        Employee employee1 = createEmployee("test@email.com");
        employeeRepository.save(employee1);
        flushAndClear();

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

    private Employee createEmployee(String email) {
        return Employee.create(
            UUID.randomUUID(),
            "홍길동",
            email,
            LocalDate.of(2020, 1, 1),
            LocalDate.of(1990, 1, 1),
            EmployeePosition.MANAGER,
            EmployeeType.FULL_TIME,
            EmployeeGrade.SENIOR,
            EmployeeAvatar.SKY_GLOW,
            "This is a memo for the employee."
        );
    }

    private Department createDepartment(String code, String name, DepartmentType type,
                                        @Nullable UUID leaderEmployeeId, @Nullable Department parent) {
        return Department.create(
            code,
            name,
            type,
            leaderEmployeeId,
            parent
        );
    }

}