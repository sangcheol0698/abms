package kr.co.abacus.abms.adapter.api.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import kr.co.abacus.abms.application.auth.outbound.AccountRepository;
import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.party.outbound.PartyRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRevenuePlanRepository;
import kr.co.abacus.abms.application.projectassignment.outbound.ProjectAssignmentRepository;
import kr.co.abacus.abms.application.permission.outbound.AccountGroupAssignmentRepository;
import kr.co.abacus.abms.application.permission.outbound.GroupPermissionGrantRepository;
import kr.co.abacus.abms.application.permission.outbound.PermissionGroupRepository;
import kr.co.abacus.abms.application.permission.outbound.PermissionRepository;
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
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignment;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignmentCreateRequest;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

@DisplayName("프로젝트 API 권한 인가")
class ProjectAuthorizationApiTest extends ApiIntegrationTestBase {

    private static final String USERNAME = "project-auth-user@abacus.co.kr";
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

    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;

    private Long departmentId;
    private Long employeeId;

    private Long partyId;

    @BeforeEach
    void setUpAccount() {
        Department department = departmentRepository.save(Department.create(
                "PROJECT-AUTH",
                "프로젝트인가부서",
                DepartmentType.TEAM,
                null,
                null
        ));

        Employee employee = employeeRepository.save(Employee.create(
                department.getIdOrThrow(),
                "프로젝트인가사용자",
                USERNAME,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(1990, 5, 20),
                EmployeePosition.ASSOCIATE,
                EmployeeType.FULL_TIME,
                EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                null
        ));
        employeeId = employee.getIdOrThrow();
        departmentId = department.getIdOrThrow();
        accountRepository.save(Account.create(employee.getIdOrThrow(), USERNAME, passwordEncoder.encode(PASSWORD)));

        partyId = partyRepository.save(Party.create(new PartyCreateRequest("프로젝트권한협력사", null, null, null, null)))
                .getIdOrThrow();
        flushAndClear();
    }

    @Test
    @DisplayName("project.read 권한이 없으면 프로젝트 목록 조회는 403을 반환한다")
    void should_forbidProjectSearchWithoutProjectReadPermission() throws Exception {
        MockHttpSession session = login();

        mockMvc.perform(get("/api/projects").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("project.read 권한이 있으면 프로젝트 목록 조회는 200을 반환한다")
    void should_allowProjectSearchWithProjectReadPermission() throws Exception {
        grantPermission("project.read", "프로젝트 조회", "프로젝트 조회 권한");
        MockHttpSession session = login();

        mockMvc.perform(get("/api/projects").session(session))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("project.read CURRENT_PARTICIPATION 권한이면 현재 참여 중인 프로젝트만 조회한다")
    void should_onlyReturnCurrentParticipatingProjects_whenGrantedCurrentParticipationScope() throws Exception {
        grantPermission("project.read", "프로젝트 조회", "프로젝트 조회 권한", PermissionScope.CURRENT_PARTICIPATION);

        Project visibleProject = projectRepository.save(Project.create(
                partyId,
                departmentId,
                "PRJ-CP-001",
                "참여중 프로젝트",
                null,
                ProjectStatus.IN_PROGRESS,
                1000000L,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(30)
        ));
        Project hiddenProject = projectRepository.save(Project.create(
                partyId,
                departmentId,
                "PRJ-CP-002",
                "미참여 프로젝트",
                null,
                ProjectStatus.IN_PROGRESS,
                1000000L,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(30)
        ));
        projectAssignmentRepository.save(ProjectAssignment.assign(
                visibleProject,
                new ProjectAssignmentCreateRequest(
                        visibleProject.getIdOrThrow(),
                        employeeId,
                        null,
                        LocalDate.now().minusDays(1),
                        LocalDate.now().plusDays(5)
                )
        ));
        flushAndClear();

        MockHttpSession session = login();

        mockMvc.perform(get("/api/projects").session(session))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.content[*].code")
                        .value(org.hamcrest.Matchers.contains("PRJ-CP-001")));

        mockMvc.perform(get("/api/projects/{id}", visibleProject.getIdOrThrow()).session(session))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/projects/{id}", hiddenProject.getIdOrThrow()).session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("project.write 권한이 없으면 프로젝트 생성은 403을 반환한다")
    void should_forbidProjectCreateWithoutProjectWritePermission() throws Exception {
        grantPermission("project.read", "프로젝트 조회", "프로젝트 조회 권한");
        MockHttpSession session = login();

        mockMvc.perform(post("/api/projects")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "partyId", partyId,
                                "leadDepartmentId", departmentId,
                                "code", "PRJ-AUTH-001",
                                "name", "권한테스트프로젝트",
                                "description", "설명",
                                "status", "SCHEDULED",
                                "contractAmount", 1000000,
                                "startDate", "2024-01-01",
                                "endDate", "2024-12-31"
                        ))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("project.write 권한이 없으면 프로젝트 매출 일정 수정은 403을 반환한다")
    void should_forbidProjectRevenuePlanUpdateWithoutProjectWritePermission() throws Exception {
        grantPermission("project.read", "프로젝트 조회", "프로젝트 조회 권한");

        Project project = projectRepository.save(Project.create(
                partyId,
                departmentId,
                "PRJ-REV-AUTH-001",
                "매출 일정 권한 프로젝트",
                null,
                ProjectStatus.IN_PROGRESS,
                1_000_000L,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        ));
        projectRevenuePlanRepository.save(ProjectRevenuePlan.create(new ProjectRevenuePlanCreateRequest(
                project.getIdOrThrow(),
                1,
                LocalDate.of(2026, 1, 15),
                RevenueType.DOWN_PAYMENT,
                100_000L,
                null
        )));
        flushAndClear();

        MockHttpSession session = login();

        mockMvc.perform(put("/api/projectRevenuePlans/{projectId}/{sequence}", project.getIdOrThrow(), 1)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "sequence", 2,
                                "revenueDate", "2026-02-20",
                                "type", "BALANCE_PAYMENT",
                                "amount", 200000,
                                "memo", "수정"
                        ))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("project.write 권한이 없으면 프로젝트 매출 일정 발행은 403을 반환한다")
    void should_forbidProjectRevenuePlanIssueWithoutProjectWritePermission() throws Exception {
        grantPermission("project.read", "프로젝트 조회", "프로젝트 조회 권한");

        Project project = projectRepository.save(Project.create(
                partyId,
                departmentId,
                "PRJ-REV-AUTH-002",
                "매출 일정 발행 프로젝트",
                null,
                ProjectStatus.IN_PROGRESS,
                1_000_000L,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        ));
        projectRevenuePlanRepository.save(ProjectRevenuePlan.create(new ProjectRevenuePlanCreateRequest(
                project.getIdOrThrow(),
                1,
                LocalDate.of(2026, 1, 15),
                RevenueType.DOWN_PAYMENT,
                100_000L,
                null
        )));
        flushAndClear();

        MockHttpSession session = login();

        mockMvc.perform(patch("/api/projectRevenuePlans/{projectId}/{sequence}/issue", project.getIdOrThrow(), 1)
                        .session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("project.write 권한이 있으면 프로젝트 생성은 200을 반환한다")
    void should_allowProjectCreateWithProjectWritePermission() throws Exception {
        grantPermission("project.write", "프로젝트 변경", "프로젝트 변경 권한");
        MockHttpSession session = login();

        mockMvc.perform(post("/api/projects")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "partyId", partyId,
                                "leadDepartmentId", departmentId,
                                "code", "PRJ-AUTH-002",
                                "name", "권한생성프로젝트",
                                "description", "설명",
                                "status", "SCHEDULED",
                                "contractAmount", 1000000,
                                "startDate", "2024-01-01",
                                "endDate", "2024-12-31"
                        ))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("project.excel.download 권한이 없으면 프로젝트 엑셀 다운로드는 403을 반환한다")
    void should_forbidProjectExcelDownloadWithoutPermission() throws Exception {
        grantPermission("project.read", "프로젝트 조회", "프로젝트 조회 권한");
        MockHttpSession session = login();

        mockMvc.perform(get("/api/projects/excel/download").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("project.excel.upload 권한이 없으면 프로젝트 엑셀 업로드는 403을 반환한다")
    void should_forbidProjectExcelUploadWithoutPermission() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "projects.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[] {1, 2, 3}
        );
        MockHttpSession session = login();

        mockMvc.perform(multipart("/api/projects/excel/upload")
                        .file(file)
                        .session(session))
                .andExpect(status().isForbidden());
    }

    private void grantPermission(String code, String name, String description) {
        grantPermission(code, name, description, PermissionScope.ALL);
    }

    private void grantPermission(String code, String name, String description, PermissionScope scope) {
        Account account = accountRepository.findByUsername(new kr.co.abacus.abms.domain.shared.Email(USERNAME)).orElseThrow();
        Permission permission = permissionRepository.save(Permission.create(code, name, description));
        PermissionGroup permissionGroup = permissionGroupRepository.save(PermissionGroup.create(
                code + " 그룹",
                code + " 권한 그룹",
                PermissionGroupType.CUSTOM
        ));
        accountGroupAssignmentRepository.save(AccountGroupAssignment.create(
                account.getIdOrThrow(),
                permissionGroup.getIdOrThrow()
        ));
        groupPermissionGrantRepository.save(GroupPermissionGrant.create(
                permissionGroup.getIdOrThrow(),
                permission.getIdOrThrow(),
                scope
        ));
        flushAndClear();
    }

    private MockHttpSession login() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "username", USERNAME,
                                "password", PASSWORD
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();
        return session;
    }

    private String toJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
