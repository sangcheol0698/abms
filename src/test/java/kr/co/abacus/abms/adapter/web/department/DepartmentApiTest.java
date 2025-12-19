package kr.co.abacus.abms.adapter.web.department;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;

import kr.co.abacus.abms.adapter.web.PageResponse;
import kr.co.abacus.abms.adapter.web.department.dto.DepartmentDetailResponse;
import kr.co.abacus.abms.adapter.web.department.dto.EmployeeAssignLeaderRequest;
import kr.co.abacus.abms.adapter.web.department.dto.OrganizationChartResponse;
import kr.co.abacus.abms.adapter.web.employee.dto.EmployeeSearchResponse;
import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
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
        Employee employee = employeeRepository.save(createEmployee("test@eamil.com"));
        flushAndClear();

        Department company = createDepartment("COMP001", "ABC Corp", DepartmentType.COMPANY, employee.getId(), null);
        Department division = createDepartment("DIV001", "ABC Corp", DepartmentType.DIVISION, null, company);
        Department team1 = createDepartment("TEAM001", "ABC Corp", DepartmentType.TEAM, null, division);
        Department team2 = createDepartment("TEAM002", "ABC Corp", DepartmentType.TEAM, null, division);
        departmentRepository.saveAll(List.of(company, division, team1, team2));
        flushAndClear();

        List<OrganizationChartResponse> responses = restTestClient.get()
            .uri("/api/departments/organization-chart")
            .exchange()
            .expectStatus().isOk()
            .expectBody(new ParameterizedTypeReference<List<OrganizationChartResponse>>() {
            })
            .returnResult()
            .getResponseBody();

        assertThat(responses).hasSize(1);
        OrganizationChartResponse response = responses.getFirst();

        assertDepartmentNode(response, company, 1);
        assertThat(response.departmentLeader().employeeName()).isEqualTo("홍길동");

        OrganizationChartResponse divisionNode = response.children().getFirst();
        assertDepartmentNode(divisionNode, division, 2);

        assertThat(divisionNode.children())
            .extracting(OrganizationChartResponse::departmentName)
            .containsExactlyInAnyOrder(team1.getName(), team2.getName());
    }

    @Test
    @DisplayName("부서 상세 정보를 조회한다")
    void getDepartment() {
        Department company = createDepartment("COMP001", "ABC Corp1", DepartmentType.COMPANY, null, null);
        Department division = createDepartment("DIV001", "ABC Corp2", DepartmentType.DIVISION, null, company);
        Department team1 = createDepartment("TEAM001", "ABC Corp3", DepartmentType.TEAM, null, division);
        departmentRepository.saveAll(List.of(company, division, team1));

        DepartmentDetailResponse response = restTestClient.get().uri("/api/departments/{id}", team1.getId())
            .exchange()
            .expectStatus().isOk()
            .expectBody(DepartmentDetailResponse.class)
            .returnResult()
            .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(team1.getId());
        assertThat(response.code()).isEqualTo(team1.getCode());
        assertThat(response.name()).isEqualTo(team1.getName());
        assertThat(response.parentDepartmentId()).isEqualTo(division.getId());
        assertThat(response.parentDepartmentName()).isEqualTo(division.getName());
    }

    @Test
    @DisplayName("부서 소속 직원들을 페이징하여 조회한다")
    void getDepartmentEmployees_withPaging() {
        // Given: 부서에 여러 직원 생성
        Department company = createDepartment("COMP001", "ABC Corp", DepartmentType.COMPANY, null, null);
        Department division = createDepartment("DIV001", "ABC Corp", DepartmentType.DIVISION, null, company);
        Department team1 = createDepartment("TEAM001", "ABC Corp", DepartmentType.TEAM, null, division);
        departmentRepository.saveAll(List.of(company, division, team1));

        for (int i = 1; i <= 15; i++) {
            Employee employee = createEmployee(team1.getId(), "employee" + i + "@email.com");
            employeeRepository.save(employee);
        }
        flushAndClear();

        // When: 첫 번째 페이지 조회 (size=10)
        PageResponse<EmployeeSearchResponse> response = restTestClient.get()
            .uri("/api/departments/{departmentId}/employees?page={page}&size={size}", team1.getId(), 0, 10)
            .exchange()
            .expectStatus().isOk()
            .expectBody(new ParameterizedTypeReference<PageResponse<EmployeeSearchResponse>>() {
            })
            .returnResult()
            .getResponseBody();

        // Then: 페이징 정보 검증
        assertThat(response.content()).hasSize(10);
        assertThat(response.totalElements()).isEqualTo(15);
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
            .body(new EmployeeAssignLeaderRequest(employee1.getId()))
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

    private Employee createEmployee(String email) {
        return Employee.create(
            UUID.randomUUID(),
            "홍길동",
            email,
            LocalDate.of(2020, 1,
                1),
            LocalDate.of(1990, 1, 1),
            EmployeePosition.MANAGER,
            EmployeeType.FULL_TIME,
            EmployeeGrade.SENIOR,
            EmployeeAvatar.SKY_GLOW,
            "This is a memo for the employee."
        );
    }

    private Employee createEmployee(UUID departmentId, String email) {
        return Employee.create(
            departmentId,
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