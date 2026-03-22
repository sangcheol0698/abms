package kr.co.abacus.abms.adapter.api.department;

import static org.assertj.core.api.Assertions.*;
import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import kr.co.abacus.abms.adapter.api.common.PageResponse;
import kr.co.abacus.abms.adapter.api.department.dto.DepartmentDetailResponse;
import kr.co.abacus.abms.adapter.api.department.dto.EmployeeAssignLeaderRequest;
import kr.co.abacus.abms.adapter.api.department.dto.OrganizationChartResponse;
import kr.co.abacus.abms.adapter.api.employee.dto.EmployeeSearchResponse;
import kr.co.abacus.abms.adapter.api.summary.dto.MonthlyRevenueSummaryResponse;
import kr.co.abacus.abms.application.auth.outbound.AccountRepository;
import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.permission.outbound.AccountGroupAssignmentRepository;
import kr.co.abacus.abms.application.permission.outbound.GroupPermissionGrantRepository;
import kr.co.abacus.abms.application.permission.outbound.PermissionGroupRepository;
import kr.co.abacus.abms.application.permission.outbound.PermissionRepository;
import kr.co.abacus.abms.application.summary.outbound.MonthlyRevenueSummaryRepository;
import kr.co.abacus.abms.domain.account.Account;
import kr.co.abacus.abms.domain.accountgroupassignment.AccountGroupAssignment;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentType;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.domain.grouppermissiongrant.GroupPermissionGrant;
import kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope;
import kr.co.abacus.abms.domain.permission.Permission;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroup;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroupType;
import kr.co.abacus.abms.domain.shared.Email;
import kr.co.abacus.abms.domain.shared.Money;
import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummary;
import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummaryCreateRequest;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

@DisplayName("부서 API (DepartmentApi)")
class DepartmentApiTest extends ApiIntegrationTestBase {

    private static final String USERNAME = "department-api-test-user@abacus.co.kr";
    private static final String PASSWORD = "Password123!";

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PermissionGroupRepository permissionGroupRepository;

    @Autowired
    private AccountGroupAssignmentRepository accountGroupAssignmentRepository;

    @Autowired
    private GroupPermissionGrantRepository groupPermissionGrantRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private MonthlyRevenueSummaryRepository monthlyRevenueSummaryRepository;

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
        Long nonExistentId = 9999L;

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
    @DisplayName("부서의 리더를 임명한다")
    void assignTeamLeader() {
        Department company = createDepartment("COMP001", "ABC Corp", DepartmentType.COMPANY, null, null);
        Department division = createDepartment("DIV001", "ABC Corp", DepartmentType.DIVISION, null, company);
        Department team1 = createDepartment("TEAM001", "ABC Corp", DepartmentType.TEAM, null, division);
        departmentRepository.saveAll(List.of(company, division, team1));

        Employee employee1 = createEmployee("test@email.com");
        employeeRepository.save(employee1);
        flushAndClear();

        accountRepository.save(Account.create(employee1.getIdOrThrow(), USERNAME,
                requireNonNull(passwordEncoder.encode(PASSWORD))));
        grantEmployeeWriteAllPermission();

        try {
            MockHttpSession session = login();

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .post("/api/departments/{departmentId}/assign-team-leader", team1.getId())
                            .session(session)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(new EmployeeAssignLeaderRequest(employee1.getId()))))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }

        flushAndClear();

        Department department = departmentRepository.findByIdAndDeletedFalse(team1.getId()).orElseThrow();

        assertThat(department.getLeaderEmployeeId()).isEqualTo(employee1.getId());
    }

    @Test
    @DisplayName("부서별 최근 6개월 매출 추이를 최신 프로젝트 스냅샷 기준으로 집계한다")
    void getDepartmentRevenueTrend() {
        Department team = createDepartment("TEAM001", "개발팀", DepartmentType.TEAM, null, null);
        Department otherTeam = createDepartment("TEAM002", "운영팀", DepartmentType.TEAM, null, null);
        departmentRepository.saveAll(List.of(team, otherTeam));
        flushAndClear();

        monthlyRevenueSummaryRepository.saveAll(List.of(
                createMonthlyRevenueSummary(team, 100L, "PRJ-100", "개발 프로젝트 A",
                        LocalDate.of(2026, 1, 31), 100_000_000L, 60_000_000L, 40_000_000L),
                createMonthlyRevenueSummary(team, 200L, "PRJ-200", "개발 프로젝트 B",
                        LocalDate.of(2026, 2, 10), 50_000_000L, 20_000_000L, 30_000_000L),
                createMonthlyRevenueSummary(team, 200L, "PRJ-200", "개발 프로젝트 B",
                        LocalDate.of(2026, 2, 20), 70_000_000L, 30_000_000L, 40_000_000L),
                createMonthlyRevenueSummary(team, 200L, "PRJ-200", "개발 프로젝트 B",
                        LocalDate.of(2026, 2, 20), 75_000_000L, 35_000_000L, 40_000_000L),
                createMonthlyRevenueSummary(team, 300L, "PRJ-300", "개발 프로젝트 C",
                        LocalDate.of(2026, 3, 15), 90_000_000L, 55_000_000L, 35_000_000L),
                createMonthlyRevenueSummary(otherTeam, 400L, "PRJ-400", "운영 프로젝트 D",
                        LocalDate.of(2026, 3, 15), 999_000_000L, 111_000_000L, 888_000_000L)
        ));
        flushAndClear();

        List<MonthlyRevenueSummaryResponse> response = restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/departments/{departmentId}/revenue/sixMonthTrend")
                        .queryParam("yearMonth", "202603")
                        .build(team.getId()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<MonthlyRevenueSummaryResponse>>() {
                })
                .returnResult()
                .getResponseBody();

        assertThat(response).hasSize(6);
        assertThat(response)
                .extracting(MonthlyRevenueSummaryResponse::targetMonth)
                .containsExactly(
                        LocalDate.of(2025, 10, 1),
                        LocalDate.of(2025, 11, 1),
                        LocalDate.of(2025, 12, 1),
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 2, 1),
                        LocalDate.of(2026, 3, 1)
                );

        assertThat(response.get(3).revenue()).isEqualByComparingTo("100000000");
        assertThat(response.get(3).cost()).isEqualByComparingTo("60000000");
        assertThat(response.get(3).profit()).isEqualByComparingTo("40000000");

        assertThat(response.get(4).revenue()).isEqualByComparingTo("75000000");
        assertThat(response.get(4).cost()).isEqualByComparingTo("35000000");
        assertThat(response.get(4).profit()).isEqualByComparingTo("40000000");

        assertThat(response.get(5).revenue()).isEqualByComparingTo("90000000");
        assertThat(response.get(5).cost()).isEqualByComparingTo("55000000");
        assertThat(response.get(5).profit()).isEqualByComparingTo("35000000");
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
                1L,
                "홍길동",
                email,
                LocalDate.of(2020, 1,
                        1),
                LocalDate.of(1990, 1, 1),
                EmployeePosition.TEAM_LEADER,
                EmployeeType.FULL_TIME,
                EmployeeGrade.SENIOR,
                EmployeeAvatar.SKY_GLOW,
                "This is a memo for the employee.");
    }

    private Employee createEmployee(Long departmentId, String email) {
        return Employee.create(
                departmentId,
                "홍길동",
                email,
                LocalDate.of(2020, 1, 1),
                LocalDate.of(1990, 1, 1),
                EmployeePosition.TEAM_LEADER,
                EmployeeType.FULL_TIME,
                EmployeeGrade.SENIOR,
                EmployeeAvatar.SKY_GLOW,
                "This is a memo for the employee.");
    }

    private Department createDepartment(String code, String name, DepartmentType type,
                                        @Nullable Long leaderEmployeeId, @Nullable Department parent) {
        return Department.create(
                code,
                name,
                type,
                leaderEmployeeId,
                parent);
    }

    private MonthlyRevenueSummary createMonthlyRevenueSummary(
            Department department,
            Long projectId,
            String projectCode,
            String projectName,
            LocalDate summaryDate,
            long revenue,
            long cost,
            long profit) {
        return MonthlyRevenueSummary.create(new MonthlyRevenueSummaryCreateRequest(
                projectId,
                projectCode,
                projectName,
                department.getIdOrThrow(),
                department.getCode(),
                department.getName(),
                summaryDate,
                Money.wons(revenue),
                Money.wons(cost),
                Money.wons(profit)
        ));
    }

    private void grantEmployeeWriteAllPermission() {
        Account account = accountRepository.findByUsername(new Email(USERNAME)).orElseThrow();
        Permission permission = permissionRepository.save(Permission.create(
                "employee.write",
                "직원 변경",
                "직원 변경 권한"
        ));
        PermissionGroup permissionGroup = permissionGroupRepository.save(PermissionGroup.create(
                "부서 API 테스트 권한 그룹",
                "부서 API 테스트 권한 그룹",
                PermissionGroupType.CUSTOM
        ));
        accountGroupAssignmentRepository.save(AccountGroupAssignment.create(
                account.getIdOrThrow(),
                permissionGroup.getIdOrThrow()
        ));
        groupPermissionGrantRepository.save(GroupPermissionGrant.create(
                permissionGroup.getIdOrThrow(),
                permission.getIdOrThrow(),
                PermissionScope.ALL
        ));
        flushAndClear();
    }

    private MockHttpSession login() throws Exception {
        MvcResult loginResult = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "username", USERNAME,
                                "password", PASSWORD
                        ))))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();
        return session;
    }

    private String toJson(Object value) {
        return objectMapper.writeValueAsString(value);
    }

}
