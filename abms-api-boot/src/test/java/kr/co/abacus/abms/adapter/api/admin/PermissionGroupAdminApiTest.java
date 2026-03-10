package kr.co.abacus.abms.adapter.api.admin;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;
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
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

@DisplayName("권한 그룹 관리 API")
class PermissionGroupAdminApiTest extends ApiIntegrationTestBase {

    private static final String ADMIN_USERNAME = "permission-admin@abacus.co.kr";
    private static final String USER_USERNAME = "permission-user@abacus.co.kr";
    private static final String PASSWORD = "Password123!";

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PermissionGroupRepository permissionGroupRepository;

    @Autowired
    private GroupPermissionGrantRepository groupPermissionGrantRepository;

    @Autowired
    private AccountGroupAssignmentRepository accountGroupAssignmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long adminAccountId;
    private Long userAccountId;
    private Long systemGroupId;
    private Long customGroupId;
    private Permission permissionManage;
    private Permission employeeRead;
    private Permission employeeWrite;

    @BeforeEach
    void setUp() {
        Department department = departmentRepository.save(Department.create(
                "PERM-ADMIN",
                "권한관리부서",
                DepartmentType.TEAM,
                null,
                null
        ));

        Employee adminEmployee = employeeRepository.save(createEmployee(
                department.getIdOrThrow(),
                ADMIN_USERNAME,
                "관리자"
        ));
        Employee userEmployee = employeeRepository.save(createEmployee(
                department.getIdOrThrow(),
                USER_USERNAME,
                "일반사용자"
        ));

        adminAccountId = accountRepository.save(Account.create(
                adminEmployee.getIdOrThrow(),
                ADMIN_USERNAME,
                passwordEncoder.encode(PASSWORD)
        )).getIdOrThrow();
        userAccountId = accountRepository.save(Account.create(
                userEmployee.getIdOrThrow(),
                USER_USERNAME,
                passwordEncoder.encode(PASSWORD)
        )).getIdOrThrow();

        permissionManage = permissionRepository.save(Permission.create(
                "permission.group.manage",
                "권한 그룹 관리",
                "권한 그룹 관리 권한"
        ));
        employeeRead = permissionRepository.save(Permission.create(
                "employee.read",
                "직원 조회",
                "직원 조회 권한"
        ));
        employeeWrite = permissionRepository.save(Permission.create(
                "employee.write",
                "직원 변경",
                "직원 변경 권한"
        ));

        PermissionGroup adminGroup = permissionGroupRepository.save(PermissionGroup.create(
                "권한 관리자 그룹",
                "권한 그룹 관리자를 위한 커스텀 그룹",
                PermissionGroupType.CUSTOM
        ));
        systemGroupId = permissionGroupRepository.save(PermissionGroup.create(
                "읽기 전용 시스템 그룹",
                "수정하면 안 되는 시스템 그룹",
                PermissionGroupType.SYSTEM
        )).getIdOrThrow();
        customGroupId = permissionGroupRepository.save(PermissionGroup.create(
                "운영 커스텀 그룹",
                "운영에 사용하는 커스텀 그룹",
                PermissionGroupType.CUSTOM
        )).getIdOrThrow();

        accountGroupAssignmentRepository.save(AccountGroupAssignment.create(adminAccountId, adminGroup.getIdOrThrow()));
        accountGroupAssignmentRepository.save(AccountGroupAssignment.create(userAccountId, customGroupId));

        groupPermissionGrantRepository.saveAll(List.of(
                GroupPermissionGrant.create(adminGroup.getIdOrThrow(), permissionManage.getIdOrThrow(), PermissionScope.ALL),
                GroupPermissionGrant.create(systemGroupId, employeeRead.getIdOrThrow(), PermissionScope.ALL),
                GroupPermissionGrant.create(customGroupId, employeeRead.getIdOrThrow(), PermissionScope.SELF)
        ));
        flushAndClear();
    }

    @Test
    @DisplayName("권한이 없으면 관리 API는 403을 반환한다")
    void should_forbidWithoutPermission() throws Exception {
        MockHttpSession session = login(USER_USERNAME);

        mockMvc.perform(get("/api/admin/permission-groups").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("권한이 있으면 그룹 목록과 상세를 조회할 수 있다")
    void should_listAndReadGroupDetail() throws Exception {
        MockHttpSession session = login(ADMIN_USERNAME);

        mockMvc.perform(get("/api/admin/permission-groups")
                        .param("name", "그룹")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItems("읽기 전용 시스템 그룹", "운영 커스텀 그룹")))
                .andExpect(jsonPath("$[?(@.name=='운영 커스텀 그룹')].assignedAccountCount").value(hasItem(1)));

        mockMvc.perform(get("/api/admin/permission-groups/{id}", customGroupId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customGroupId))
                .andExpect(jsonPath("$.groupType").value("CUSTOM"))
                .andExpect(jsonPath("$.grants[0].permissionCode").value("employee.read"))
                .andExpect(jsonPath("$.accounts[0].accountId").value(userAccountId));
    }

    @Test
    @DisplayName("권한 카탈로그와 추가 가능한 계정을 조회할 수 있다")
    void should_getCatalogAndAssignableAccounts() throws Exception {
        MockHttpSession session = login(ADMIN_USERNAME);

        mockMvc.perform(get("/api/admin/permission-groups/catalog").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissions[*].code", hasItems(
                        "permission.group.manage",
                        "employee.read",
                        "employee.write")))
                .andExpect(jsonPath("$.scopes[*].code", hasItems(
                        "ALL",
                        "OWN_DEPARTMENT",
                        "OWN_DEPARTMENT_TREE",
                        "SELF")));

        mockMvc.perform(get("/api/admin/accounts")
                        .param("permissionGroupId", String.valueOf(customGroupId))
                        .param("keyword", "관리자")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email", contains(ADMIN_USERNAME)));
    }

    @Test
    @DisplayName("커스텀 그룹은 생성하고 수정할 수 있다")
    void should_createAndUpdateCustomGroup() throws Exception {
        MockHttpSession session = login(ADMIN_USERNAME);

        MvcResult createResult = mockMvc.perform(post("/api/admin/permission-groups")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "name", "신규 권한 그룹",
                                "description", "새로운 권한 그룹입니다.",
                                "grants", List.of(
                                        Map.of("permissionCode", "employee.read", "scopes", List.of("SELF", "OWN_DEPARTMENT")),
                                        Map.of("permissionCode", "employee.write", "scopes", List.of("SELF"))
                                )
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        Long createdGroupId = Long.parseLong(createResult.getResponse().getContentAsString());

        mockMvc.perform(put("/api/admin/permission-groups/{id}", createdGroupId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "name", "수정된 권한 그룹",
                                "description", "수정된 설명입니다.",
                                "grants", List.of(
                                        Map.of("permissionCode", "employee.read", "scopes", List.of("ALL"))
                                )
                        ))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/permission-groups/{id}", createdGroupId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("수정된 권한 그룹"))
                .andExpect(jsonPath("$.grants", hasSize(1)))
                .andExpect(jsonPath("$.grants[0].permissionCode").value("employee.read"))
                .andExpect(jsonPath("$.grants[0].scopes[0]").value("ALL"));
    }

    @Test
    @DisplayName("시스템 그룹은 수정하거나 삭제할 수 없다")
    void should_forbidSystemGroupMutation() throws Exception {
        MockHttpSession session = login(ADMIN_USERNAME);

        mockMvc.perform(put("/api/admin/permission-groups/{id}", systemGroupId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "name", "시스템 그룹 수정",
                                "description", "수정 시도",
                                "grants", List.of(
                                        Map.of("permissionCode", "employee.read", "scopes", List.of("ALL"))
                                )
                        ))))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/admin/permission-groups/{id}", systemGroupId).session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("계정을 그룹에 할당하고 해제할 수 있다")
    void should_assignAndUnassignAccount() throws Exception {
        MockHttpSession session = login(ADMIN_USERNAME);

        mockMvc.perform(post("/api/admin/permission-groups/{id}/accounts", systemGroupId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("accountId", adminAccountId))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/permission-groups/{id}", systemGroupId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts[*].accountId", hasItem(adminAccountId.intValue())));

        mockMvc.perform(delete("/api/admin/permission-groups/{id}/accounts/{accountId}", systemGroupId, adminAccountId)
                        .session(session))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/permission-groups/{id}", systemGroupId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts[*].accountId", not(hasItem(adminAccountId.intValue()))));
    }

    @Test
    @DisplayName("커스텀 그룹 삭제 시 활성 할당도 함께 비활성화된다")
    void should_softDeleteAssignmentsWhenDeletingCustomGroup() throws Exception {
        MockHttpSession session = login(ADMIN_USERNAME);

        mockMvc.perform(delete("/api/admin/permission-groups/{id}", customGroupId).session(session))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/permission-groups/{id}", customGroupId).session(session))
                .andExpect(status().isNotFound());

        assertThat(accountGroupAssignmentRepository
                .findByAccountIdAndPermissionGroupIdAndDeletedFalse(userAccountId, customGroupId)).isEmpty();
        assertThat(accountGroupAssignmentRepository.findByAccountIdAndPermissionGroupId(userAccountId, customGroupId))
                .isPresent()
                .get()
                .satisfies(assignment -> assertThat(assignment.isDeleted()).isTrue());
    }

    private MockHttpSession login(String username) throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "username", username,
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
                LocalDate.of(1990, 1, 1),
                EmployeePosition.ASSOCIATE,
                EmployeeType.FULL_TIME,
                EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                null
        );
    }
}
