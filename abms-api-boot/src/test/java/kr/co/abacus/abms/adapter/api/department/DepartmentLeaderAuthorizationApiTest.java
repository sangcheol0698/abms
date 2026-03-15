package kr.co.abacus.abms.adapter.api.department;

import static org.assertj.core.api.Assertions.assertThat;
import static java.util.Objects.requireNonNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

@DisplayName("부서 리더 변경 권한 인가")
class DepartmentLeaderAuthorizationApiTest extends ApiIntegrationTestBase {

    private static final String USERNAME = "department-leader-auth-user@abacus.co.kr";
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

    private Long ownDivisionId;
    private Long childTeamId;
    private Long outsideDivisionId;

    private Long ownDivisionLeaderId;
    private Long childTeamLeaderId;
    private Long outsideDivisionLeaderId;

    @BeforeEach
    void setUpAccount() {
        Department ownDivision = departmentRepository.save(Department.create(
                "LEADER-AUTH-OWN",
                "권한 본부",
                DepartmentType.DIVISION,
                null,
                null
        ));
        Department childTeam = departmentRepository.save(Department.create(
                "LEADER-AUTH-CHILD",
                "권한 팀",
                DepartmentType.TEAM,
                null,
                ownDivision
        ));
        Department outsideDivision = departmentRepository.save(Department.create(
                "LEADER-AUTH-OUTSIDE",
                "외부 본부",
                DepartmentType.DIVISION,
                null,
                null
        ));

        ownDivisionId = ownDivision.getIdOrThrow();
        childTeamId = childTeam.getIdOrThrow();
        outsideDivisionId = outsideDivision.getIdOrThrow();

        Employee currentEmployee = employeeRepository.save(createEmployee(ownDivisionId, USERNAME, "인가사용자"));
        accountRepository.save(Account.create(currentEmployee.getIdOrThrow(), USERNAME,
                requireNonNull(passwordEncoder.encode(PASSWORD))));

        ownDivisionLeaderId = employeeRepository.save(createEmployee(
                ownDivisionId,
                "own-leader@abacus.co.kr",
                "본부리더"
        )).getIdOrThrow();

        childTeamLeaderId = employeeRepository.save(createEmployee(
                childTeamId,
                "child-leader@abacus.co.kr",
                "하위리더"
        )).getIdOrThrow();

        outsideDivisionLeaderId = employeeRepository.save(createEmployee(
                outsideDivisionId,
                "outside-leader@abacus.co.kr",
                "외부리더"
        )).getIdOrThrow();

        flushAndClear();
    }

    @Test
    @DisplayName("employee.write 권한이 없으면 리더 변경은 403을 반환한다")
    void should_forbidLeaderAssignmentWithoutEmployeeWritePermission() throws Exception {
        MockHttpSession session = login();

        assignLeader(ownDivisionId, ownDivisionLeaderId, session)
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SELF 범위 employee.write 권한은 리더 변경을 허용하지 않는다")
    void should_forbidLeaderAssignmentWithSelfScope() throws Exception {
        grantEmployeeWritePermission(PermissionScope.SELF);
        MockHttpSession session = login();

        assignLeader(ownDivisionId, ownDivisionLeaderId, session)
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("OWN_DEPARTMENT 범위 employee.write 권한은 본인 부서 리더 변경만 허용한다")
    void should_allowOwnDepartmentOnlyWithOwnDepartmentScope() throws Exception {
        grantEmployeeWritePermission(PermissionScope.OWN_DEPARTMENT);
        MockHttpSession session = login();

        assignLeader(ownDivisionId, ownDivisionLeaderId, session)
                .andExpect(status().isOk());
        flushAndClear();
        Department ownDivision = departmentRepository.findByIdAndDeletedFalse(ownDivisionId).orElseThrow();
        assertThat(ownDivision.getLeaderEmployeeId()).isEqualTo(ownDivisionLeaderId);

        assignLeader(childTeamId, childTeamLeaderId, session)
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("OWN_DEPARTMENT_TREE 범위 employee.write 권한은 하위 부서 리더 변경을 허용한다")
    void should_allowChildDepartmentWithOwnDepartmentTreeScope() throws Exception {
        grantEmployeeWritePermission(PermissionScope.OWN_DEPARTMENT_TREE);
        MockHttpSession session = login();

        assignLeader(childTeamId, childTeamLeaderId, session)
                .andExpect(status().isOk());
        flushAndClear();
        Department childTeam = departmentRepository.findByIdAndDeletedFalse(childTeamId).orElseThrow();
        assertThat(childTeam.getLeaderEmployeeId()).isEqualTo(childTeamLeaderId);

        assignLeader(outsideDivisionId, outsideDivisionLeaderId, session)
                .andExpect(status().isForbidden());
    }

    private org.springframework.test.web.servlet.ResultActions assignLeader(
            Long departmentId,
            Long leaderEmployeeId,
            MockHttpSession session
    ) throws Exception {
        return mockMvc.perform(post("/api/departments/{departmentId}/assign-team-leader", departmentId)
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("leaderEmployeeId", leaderEmployeeId))));
    }

    private void grantEmployeeWritePermission(PermissionScope... scopes) {
        Account account = accountRepository.findByUsername(new Email(USERNAME)).orElseThrow();
        Permission permission = permissionRepository.save(Permission.create(
                "employee.write",
                "직원 변경",
                "직원 변경 권한"
        ));
        PermissionGroup permissionGroup = permissionGroupRepository.save(PermissionGroup.create(
                "부서 리더 변경 권한 그룹",
                "부서 리더 변경 권한 그룹",
                PermissionGroupType.CUSTOM
        ));
        accountGroupAssignmentRepository.save(AccountGroupAssignment.create(
                account.getIdOrThrow(),
                permissionGroup.getIdOrThrow()
        ));
        for (PermissionScope scope : scopes) {
            groupPermissionGrantRepository.save(GroupPermissionGrant.create(
                    permissionGroup.getIdOrThrow(),
                    permission.getIdOrThrow(),
                    scope
            ));
        }
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

    private String toJson(Object value) {
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

