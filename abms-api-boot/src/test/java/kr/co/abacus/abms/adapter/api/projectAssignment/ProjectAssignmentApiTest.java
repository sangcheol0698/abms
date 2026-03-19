package kr.co.abacus.abms.adapter.api.projectAssignment;

import static org.assertj.core.api.Assertions.*;

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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import kr.co.abacus.abms.application.auth.outbound.AccountRepository;
import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.party.outbound.PartyRepository;
import kr.co.abacus.abms.application.permission.outbound.AccountGroupAssignmentRepository;
import kr.co.abacus.abms.application.permission.outbound.GroupPermissionGrantRepository;
import kr.co.abacus.abms.application.permission.outbound.PermissionGroupRepository;
import kr.co.abacus.abms.application.permission.outbound.PermissionRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.application.projectassignment.outbound.ProjectAssignmentRepository;
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
import kr.co.abacus.abms.domain.project.ProjectStatus;
import kr.co.abacus.abms.domain.projectassignment.AssignmentRole;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignment;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignmentCreateRequest;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

@DisplayName("프로젝트 투입 API (ProjectAssignmentApi)")
class ProjectAssignmentApiTest extends ApiIntegrationTestBase {

    private static final String USERNAME = "project-assignment-api-user@abacus.co.kr";
    private static final String PASSWORD = "Password123!";

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PermissionGroupRepository permissionGroupRepository;

    @Autowired
    private AccountGroupAssignmentRepository accountGroupAssignmentRepository;

    @Autowired
    private GroupPermissionGrantRepository groupPermissionGrantRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;

    private Long projectId;
    private Long assignmentId;

    @BeforeEach
    void setUp() {
        Department managingDepartment = departmentRepository.save(Department.create(
                "ASSIGN-API",
                "투입API부서",
                DepartmentType.TEAM,
                null,
                null
        ));
        Employee apiUser = employeeRepository.save(Employee.create(
                managingDepartment.getIdOrThrow(),
                "투입API사용자",
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
                apiUser.getIdOrThrow(),
                USERNAME,
                passwordEncoder.encode(PASSWORD)
        ));

        PermissionGroup permissionGroup = permissionGroupRepository.save(PermissionGroup.create(
                "프로젝트 투입 API 권한 그룹",
                "프로젝트 투입 API 테스트용 권한 그룹",
                PermissionGroupType.CUSTOM
        ));
        accountGroupAssignmentRepository.save(AccountGroupAssignment.create(
                account.getIdOrThrow(),
                permissionGroup.getIdOrThrow()
        ));

        grant(permissionGroup, "project.read", "프로젝트 조회");
        grant(permissionGroup, "project.write", "프로젝트 변경");

        Department assignmentDepartment = departmentRepository.save(Department.create(
                "BACKEND",
                "백엔드팀",
                DepartmentType.TEAM,
                null,
                null
        ));
        Employee assignedEmployee = employeeRepository.save(Employee.create(
                assignmentDepartment.getIdOrThrow(),
                "배정직원",
                "assigned@abms.co.kr",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(1992, 3, 10),
                EmployeePosition.PRINCIPAL,
                EmployeeType.FULL_TIME,
                EmployeeGrade.MID_LEVEL,
                EmployeeAvatar.SKY_GLOW,
                null
        ));
        Party party = partyRepository.save(Party.create(new PartyCreateRequest("협력사A", null, null, null, null)));
        Project project = projectRepository.save(Project.create(
                party.getId(),
                managingDepartment.getIdOrThrow(),
                "PRJ-ASSIGN-001",
                "투입 테스트 프로젝트",
                null,
                ProjectStatus.IN_PROGRESS,
                100_000_000L,
                LocalDate.now().minusMonths(1),
                LocalDate.now().plusMonths(2)
        ));
        ProjectAssignment assignment = projectAssignmentRepository.save(ProjectAssignment.assign(
                project,
                new ProjectAssignmentCreateRequest(
                        project.getIdOrThrow(),
                        assignedEmployee.getIdOrThrow(),
                        AssignmentRole.DEV,
                        LocalDate.now().minusDays(10),
                        LocalDate.now().plusDays(10)
                )
        ));

        projectId = project.getIdOrThrow();
        assignmentId = assignment.getIdOrThrow();
        flushAndClear();
    }

    @Test
    @DisplayName("프로젝트 투입 조회 응답에 직원명, 부서명, 상태가 포함된다")
    void findByProjectId() throws Exception {
        MockHttpSession session = login();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/project-assignments")
                        .session(session)
                        .param("projectId", String.valueOf(projectId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].employeeName").value("배정직원"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].departmentName").value("백엔드팀"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].assignmentStatus").value("CURRENT"));
    }

    @Test
    @DisplayName("프로젝트 투입 정보를 수정한다")
    void update() throws Exception {
        MockHttpSession session = login();

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/project-assignments/{id}", assignmentId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "employeeId", projectAssignmentRepository.findById(assignmentId).orElseThrow().getEmployeeId(),
                                "role", "PM",
                                "startDate", LocalDate.now().minusDays(5),
                                "endDate", LocalDate.now().plusDays(5)
                        ))))
                .andExpect(MockMvcResultMatchers.status().isOk());

        flushAndClear();
        ProjectAssignment updated = projectAssignmentRepository.findById(assignmentId).orElseThrow();
        assertThat(updated.getRole()).isEqualTo(AssignmentRole.PM);
        assertThat(updated.getPeriod().startDate()).isEqualTo(LocalDate.now().minusDays(5));
    }

    @Test
    @DisplayName("프로젝트 투입 종료일을 수정해 종료 처리한다")
    void end() throws Exception {
        MockHttpSession session = login();

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/project-assignments/{id}/end", assignmentId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "endDate", LocalDate.now().minusDays(1)
                        ))))
                .andExpect(MockMvcResultMatchers.status().isOk());

        flushAndClear();
        ProjectAssignment ended = projectAssignmentRepository.findById(assignmentId).orElseThrow();
        assertThat(ended.getPeriod().endDate()).isEqualTo(LocalDate.now().minusDays(1));
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
        MvcResult loginResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", USERNAME,
                                "password", PASSWORD
                        ))))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();
        return session;
    }
}
