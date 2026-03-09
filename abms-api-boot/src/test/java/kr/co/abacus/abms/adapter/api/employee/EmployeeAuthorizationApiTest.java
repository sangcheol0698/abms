package kr.co.abacus.abms.adapter.api.employee;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import kr.co.abacus.abms.domain.permission.Permission;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroup;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroupType;
import kr.co.abacus.abms.domain.shared.Email;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

@DisplayName("직원 API 권한 인가")
class EmployeeAuthorizationApiTest extends ApiIntegrationTestBase {

    private static final String USERNAME = "employee-auth-user@abacus.co.kr";
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

    private Long teamId;

    @BeforeEach
    void setUpAccount() {
        Department team = departmentRepository.save(Department.create(
                "AUTH-TEAM",
                "인가 테스트팀",
                DepartmentType.TEAM,
                null,
                null
        ));
        teamId = team.getIdOrThrow();

        Employee employee = employeeRepository.save(createEmployee(teamId, USERNAME, "인가사용자"));
        accountRepository.save(Account.create(employee.getIdOrThrow(), USERNAME, passwordEncoder.encode(PASSWORD)));
        flushAndClear();
    }

    @Test
    @DisplayName("로그인하지 않으면 직원 조회 API는 401을 반환한다")
    void should_requireAuthentication_whenReadingEmployeePositions() throws Exception {
        mockMvc.perform(get("/api/employees/positions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그인했지만 employee.read 권한이 없으면 직원 조회 API는 403을 반환한다")
    void should_returnForbidden_whenAuthenticatedWithoutEmployeeReadPermission() throws Exception {
        MockHttpSession session = login();

        mockMvc.perform(get("/api/employees/positions").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("employee.read SELF 권한이 있으면 직원 조회 API에 접근할 수 있다")
    void should_allowEmployeeReadApis_whenGrantedSelfScope() throws Exception {
        grantEmployeeReadPermission(PermissionScope.SELF);
        MockHttpSession session = login();

        mockMvc.perform(get("/api/employees/positions").session(session))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("employee.read OWN_DEPARTMENT 권한이 있으면 직원 조회 API에 접근할 수 있다")
    void should_allowEmployeeReadApis_whenGrantedOwnDepartmentScope() throws Exception {
        grantEmployeeReadPermission(PermissionScope.OWN_DEPARTMENT);
        MockHttpSession session = login();

        mockMvc.perform(get("/api/employees/positions").session(session))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("employee.read 권한이 있으면 직원 목록과 상세를 조회할 수 있다")
    void should_allowEmployeeSearchAndDetail_whenGrantedEmployeeReadPermission() throws Exception {
        grantEmployeeReadPermission(PermissionScope.ALL);
        Employee targetEmployee = employeeRepository.save(createEmployee(
                teamId,
                "target-employee@abacus.co.kr",
                "조회대상직원"
        ));
        flushAndClear();

        MockHttpSession session = login();

        mockMvc.perform(get("/api/employees")
                        .param("page", "0")
                        .param("size", "20")
                        .session(session))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/employees/{id}", targetEmployee.getIdOrThrow()).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(targetEmployee.getIdOrThrow()))
                .andExpect(jsonPath("$.name").value("조회대상직원"));
    }

    private void grantEmployeeReadPermission(PermissionScope scope) {
        Account account = accountRepository.findByUsername(new Email(USERNAME)).orElseThrow();
        Permission permission = permissionRepository.save(Permission.create(
                "employee.read",
                "직원 조회",
                "직원 조회 권한"
        ));
        PermissionGroup permissionGroup = permissionGroupRepository.save(PermissionGroup.create(
                "직원 조회 그룹 " + scope.name(),
                "직원 조회 권한 그룹",
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

    private Employee createEmployee(Long departmentId, String email, String name) {
        return Employee.create(
                departmentId,
                name,
                email,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(1990, 5, 20),
                EmployeePosition.ASSOCIATE,
                EmployeeType.FULL_TIME,
                EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                null
        );
    }

}
