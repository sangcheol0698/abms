package kr.co.abacus.abms.adapter.api.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import kr.co.abacus.abms.application.auth.outbound.AccountRepository;
import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.party.outbound.PartyRepository;
import kr.co.abacus.abms.application.permission.outbound.AccountGroupAssignmentRepository;
import kr.co.abacus.abms.application.permission.outbound.GroupPermissionGrantRepository;
import kr.co.abacus.abms.application.permission.outbound.PermissionGroupRepository;
import kr.co.abacus.abms.application.permission.outbound.PermissionRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRevenuePlanRepository;
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
import kr.co.abacus.abms.domain.party.Party;
import kr.co.abacus.abms.domain.party.PartyCreateRequest;
import kr.co.abacus.abms.domain.permission.Permission;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroup;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroupType;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlan;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlanCreateRequest;
import kr.co.abacus.abms.domain.project.ProjectStatus;
import kr.co.abacus.abms.domain.project.RevenueType;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

@DisplayName("프로젝트 매출 계획 API (ProjectRevenuePlanApi)")
class ProjectRevenuePlanApiTest extends ApiIntegrationTestBase {

    private static final String USERNAME = "project-revenue-api-user@abacus.co.kr";
    private static final String PASSWORD = "Password123!";

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PermissionGroupRepository permissionGroupRepository;

    @Autowired
    private AccountGroupAssignmentRepository accountGroupAssignmentRepository;

    @Autowired
    private GroupPermissionGrantRepository groupPermissionGrantRepository;

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectRevenuePlanRepository projectRevenuePlanRepository;

    private Long departmentId;
    private Long partyId;

    @BeforeEach
    void setUpAccount() {
        Department department = departmentRepository.save(Department.create(
                "PROJECT-REVENUE-API",
                "프로젝트매출일정부서",
                DepartmentType.TEAM,
                null,
                null
        ));

        Employee employee = employeeRepository.save(Employee.create(
                department.getIdOrThrow(),
                "매출사용자",
                USERNAME,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(1990, 5, 20),
                EmployeePosition.ASSOCIATE,
                EmployeeType.FULL_TIME,
                EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                null
        ));
        Account account = accountRepository.save(Account.create(
                employee.getIdOrThrow(),
                USERNAME,
                passwordEncoder.encode(PASSWORD)
        ));

        PermissionGroup permissionGroup = permissionGroupRepository.save(PermissionGroup.create(
                "프로젝트 매출 일정 API 권한 그룹",
                "프로젝트 매출 일정 API 테스트용 권한 그룹",
                PermissionGroupType.CUSTOM
        ));
        accountGroupAssignmentRepository.save(AccountGroupAssignment.create(
                account.getIdOrThrow(),
                permissionGroup.getIdOrThrow()
        ));

        grant(permissionGroup, "project.read", "프로젝트 조회");
        grant(permissionGroup, "project.write", "프로젝트 변경");

        departmentId = department.getIdOrThrow();
        partyId = partyRepository.save(Party.create(new PartyCreateRequest("프로젝트매출협력사", null, null, null, null)))
                .getIdOrThrow();
        flushAndClear();
    }

    @Test
    @DisplayName("프로젝트 매출 계획 수정")
    void update() throws Exception {
        Project project = createProject("PRJ-REV-001");
        projectRevenuePlanRepository.save(ProjectRevenuePlan.create(new ProjectRevenuePlanCreateRequest(
                project.getIdOrThrow(),
                1,
                LocalDate.of(2026, 1, 15),
                RevenueType.DOWN_PAYMENT,
                30_000_000L,
                "수정 전"
        )));
        flushAndClear();

        MockHttpSession session = login();
        MvcResult result = mockMvc.perform(put("/api/projectRevenuePlans/{projectId}/{sequence}", project.getIdOrThrow(), 1)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "sequence", 2,
                                "revenueDate", "2026-02-20",
                                "type", "BALANCE_PAYMENT",
                                "amount", 45_000_000L,
                                "memo", "수정 후"
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), Map.class);
        assertThat(response.get("projectId")).isEqualTo(project.getIdOrThrow().intValue());
        assertThat(response.get("sequence")).isEqualTo(2);
        assertThat(response.get("revenueDate")).isEqualTo("2026-02-20");
        assertThat(response.get("type")).isEqualTo("BALANCE_PAYMENT");
        assertThat(response.get("amount")).isEqualTo(45_000_000);
        assertThat(response.get("status")).isEqualTo("PLANNED");
        assertThat(response.get("memo")).isEqualTo("수정 후");

        flushAndClear();
        ProjectRevenuePlan savedPlan = projectRevenuePlanRepository.findByProjectIdAndSequence(project.getIdOrThrow(), 2).orElseThrow();
        assertThat(savedPlan.getMemo()).isEqualTo("수정 후");
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트 매출 계획 수정은 404를 반환한다")
    void update_notFound() throws Exception {
        Project project = createProject("PRJ-REV-002");
        flushAndClear();

        MockHttpSession session = login();
        mockMvc.perform(put("/api/projectRevenuePlans/{projectId}/{sequence}", project.getIdOrThrow(), 99)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "sequence", 100,
                                "revenueDate", "2026-02-20",
                                "type", "BALANCE_PAYMENT",
                                "amount", 45_000_000L,
                                "memo", "수정 후"
                        ))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("프로젝트 매출 계획 발행")
    void issue() throws Exception {
        Project project = createProject("PRJ-REV-003");
        projectRevenuePlanRepository.save(ProjectRevenuePlan.create(new ProjectRevenuePlanCreateRequest(
                project.getIdOrThrow(),
                1,
                LocalDate.of(2026, 1, 15),
                RevenueType.DOWN_PAYMENT,
                30_000_000L,
                "발행 전"
        )));
        flushAndClear();

        MockHttpSession session = login();
        MvcResult result = mockMvc.perform(patch("/api/projectRevenuePlans/{projectId}/{sequence}/issue", project.getIdOrThrow(), 1)
                        .session(session))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), Map.class);
        assertThat(response.get("status")).isEqualTo("INVOICED");
        assertThat(projectRevenuePlanRepository.findByProjectIdAndSequence(project.getIdOrThrow(), 1).orElseThrow().getIsIssued()).isTrue();
    }

    @Test
    @DisplayName("프로젝트 매출 계획 발행 취소")
    void cancel() throws Exception {
        Project project = createProject("PRJ-REV-004");
        ProjectRevenuePlan projectRevenuePlan = ProjectRevenuePlan.create(new ProjectRevenuePlanCreateRequest(
                project.getIdOrThrow(),
                1,
                LocalDate.of(2026, 1, 15),
                RevenueType.DOWN_PAYMENT,
                30_000_000L,
                "발행 후"
        ));
        projectRevenuePlan.issue();
        projectRevenuePlanRepository.save(projectRevenuePlan);
        flushAndClear();

        MockHttpSession session = login();
        MvcResult result = mockMvc.perform(patch("/api/projectRevenuePlans/{projectId}/{sequence}/cancel", project.getIdOrThrow(), 1)
                        .session(session))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), Map.class);
        assertThat(response.get("status")).isEqualTo("PLANNED");
        assertThat(projectRevenuePlanRepository.findByProjectIdAndSequence(project.getIdOrThrow(), 1).orElseThrow().getIsIssued()).isFalse();
    }

    private Project createProject(String code) {
        return projectRepository.save(Project.create(
                partyId,
                departmentId,
                code,
                "테스트 매출 일정 프로젝트",
                null,
                ProjectStatus.IN_PROGRESS,
                100_000_000L,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        ));
    }

    private void grant(PermissionGroup permissionGroup, String code, String name) {
        Permission permission = permissionRepository.save(Permission.create(
                code,
                name,
                name + " 권한"
        ));
        groupPermissionGrantRepository.save(GroupPermissionGrant.create(
                permissionGroup.getIdOrThrow(),
                permission.getIdOrThrow(),
                PermissionScope.ALL
        ));
    }

    private MockHttpSession login() throws Exception {
        MvcResult loginResult = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", USERNAME,
                                "password", PASSWORD
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();
        return session;
    }

}
