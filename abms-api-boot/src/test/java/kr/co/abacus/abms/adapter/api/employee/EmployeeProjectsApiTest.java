package kr.co.abacus.abms.adapter.api.employee;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.Set;

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

@DisplayName("직원 프로젝트 API")
class EmployeeProjectsApiTest extends ApiIntegrationTestBase {

    private static final String USERNAME = "employee-projects-user@abacus.co.kr";
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
    private PartyRepository partyRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PermissionGroupRepository permissionGroupRepository;

    @Autowired
    private AccountGroupAssignmentRepository accountGroupAssignmentRepository;

    @Autowired
    private GroupPermissionGrantRepository groupPermissionGrantRepository;

    private Long ownDepartmentId;
    private Long outsideDepartmentId;
    private Long employeeId;

    @BeforeEach
    void setUp() {
        Department ownDepartment = departmentRepository.save(Department.create(
                "EMP-PROJ-OWN",
                "직원 프로젝트 본부",
                DepartmentType.DIVISION,
                null,
                null
        ));
        Department outsideDepartment = departmentRepository.save(Department.create(
                "EMP-PROJ-OUT",
                "외부 본부",
                DepartmentType.DIVISION,
                null,
                null
        ));

        ownDepartmentId = ownDepartment.getIdOrThrow();
        outsideDepartmentId = outsideDepartment.getIdOrThrow();

        Employee employee = employeeRepository.save(Employee.create(
                ownDepartmentId,
                "직원프로젝트대상",
                "target@abacus.co.kr",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(1990, 1, 1),
                EmployeePosition.ASSOCIATE,
                EmployeeType.FULL_TIME,
                EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                null
        ));
        employeeId = employee.getIdOrThrow();

        Employee currentEmployee = employeeRepository.save(Employee.create(
                ownDepartmentId,
                "직원프로젝트조회자",
                USERNAME,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(1990, 1, 1),
                EmployeePosition.ASSOCIATE,
                EmployeeType.FULL_TIME,
                EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                null
        ));
        accountRepository.save(Account.create(currentEmployee.getIdOrThrow(), USERNAME, passwordEncoder.encode(PASSWORD)));
        flushAndClear();
    }

    @Test
    @DisplayName("project.read 권한이 없으면 직원 프로젝트 조회는 403을 반환한다")
    void forbidWithoutProjectReadPermission() throws Exception {
        grantPermission("employee.read", PermissionScope.ALL);
        MockHttpSession session = login();

        mockMvc.perform(get("/api/employees/{id}/projects", employeeId).session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("권한 범위 밖 프로젝트는 제외하고 시작일 역순으로 반환한다")
    void filterAndSortProjectsByPermission() throws Exception {
        grantPermission("employee.read", PermissionScope.ALL);
        grantPermission("project.read", PermissionScope.OWN_DEPARTMENT);

        Party party = partyRepository.save(Party.create(new PartyCreateRequest("고객사", null, null, null, null)));
        LocalDate today = LocalDate.now();

        Project visibleCurrentProject = projectRepository.save(Project.create(
                party.getId(), ownDepartmentId, "PRJ-CURRENT", "현재 프로젝트", null,
                ProjectStatus.IN_PROGRESS, 1000000L, today.minusMonths(1), today.plusMonths(1)
        ));
        Project visibleScheduledProject = projectRepository.save(Project.create(
                party.getId(), ownDepartmentId, "PRJ-NEXT", "예정 프로젝트", null,
                ProjectStatus.SCHEDULED, 1000000L, today.plusDays(1), today.plusMonths(2)
        ));
        Project hiddenProject = projectRepository.save(Project.create(
                party.getId(), outsideDepartmentId, "PRJ-HIDDEN", "숨김 프로젝트", null,
                ProjectStatus.IN_PROGRESS, 1000000L, today.minusMonths(1), today.plusMonths(1)
        ));

        projectAssignmentRepository.save(ProjectAssignment.assign(visibleCurrentProject, new ProjectAssignmentCreateRequest(
                visibleCurrentProject.getIdOrThrow(), employeeId, AssignmentRole.DEV, today.minusDays(10), today.plusDays(10)
        )));
        projectAssignmentRepository.save(ProjectAssignment.assign(visibleScheduledProject, new ProjectAssignmentCreateRequest(
                visibleScheduledProject.getIdOrThrow(), employeeId, AssignmentRole.PM, today.plusDays(1), today.plusDays(20)
        )));
        projectAssignmentRepository.save(ProjectAssignment.assign(hiddenProject, new ProjectAssignmentCreateRequest(
                hiddenProject.getIdOrThrow(), employeeId, AssignmentRole.PL, today.minusDays(5), today.plusDays(5)
        )));
        flushAndClear();

        MockHttpSession session = login();

        mockMvc.perform(get("/api/employees/{id}/projects", employeeId)
                        .param("page", "0")
                        .param("size", "10")
                        .param("name", "프로젝트")
                        .param("assignmentStatuses", "CURRENT,SCHEDULED")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].projectCode").value("PRJ-NEXT"))
                .andExpect(jsonPath("$.content[0].assignmentStatus").value("SCHEDULED"))
                .andExpect(jsonPath("$.content[1].projectCode").value("PRJ-CURRENT"))
                .andExpect(jsonPath("$.content[1].assignmentStatus").value("CURRENT"))
                .andExpect(jsonPath("$.content[*].projectCode", not(hasItem("PRJ-HIDDEN"))))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    private void grantPermission(String code, PermissionScope scope) {
        Account account = accountRepository.findByUsername(new kr.co.abacus.abms.domain.shared.Email(USERNAME))
                .orElseThrow();
        Permission permission = permissionRepository.findByCodeAndDeletedFalse(code)
                .orElseGet(() -> permissionRepository.save(Permission.create(
                        code,
                        code,
                        code + " 권한"
                )));

        PermissionGroup permissionGroup = permissionGroupRepository.save(PermissionGroup.create(
                code + "-employee-projects-group",
                code + " 그룹",
                PermissionGroupType.CUSTOM
        ));

        accountGroupAssignmentRepository.save(AccountGroupAssignment.create(account.getIdOrThrow(), permissionGroup.getIdOrThrow()));
        groupPermissionGrantRepository.saveAll(
                Set.of(scope).stream()
                        .map(grantScope -> GroupPermissionGrant.create(
                                permissionGroup.getIdOrThrow(),
                                permission.getIdOrThrow(),
                                grantScope
                        ))
                        .toList()
        );
        flushAndClear();
    }

    private MockHttpSession login() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "username", USERNAME,
                                "password", PASSWORD
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        return (MockHttpSession) loginResult.getRequest().getSession(false);
    }
}
