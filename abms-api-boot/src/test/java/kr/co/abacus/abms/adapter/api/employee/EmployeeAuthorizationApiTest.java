package kr.co.abacus.abms.adapter.api.employee;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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

    private Long ownDivisionId;
    private Long childTeamId;
    private Long outsideDivisionId;
    private Long selfEmployeeId;
    private Long sameDepartmentEmployeeId;
    private Long childDepartmentEmployeeId;
    private Long outsideDepartmentEmployeeId;

    @BeforeEach
    void setUpAccount() {
        Department ownDivision = departmentRepository.save(Department.create(
                "AUTH-DIVISION",
                "인가 본부",
                DepartmentType.DIVISION,
                null,
                null
        ));
        Department childTeam = departmentRepository.save(Department.create(
                "AUTH-TEAM",
                "인가 팀",
                DepartmentType.TEAM,
                null,
                ownDivision
        ));
        Department outsideDivision = departmentRepository.save(Department.create(
                "AUTH-OUTSIDE",
                "다른 본부",
                DepartmentType.DIVISION,
                null,
                null
        ));

        ownDivisionId = ownDivision.getIdOrThrow();
        childTeamId = childTeam.getIdOrThrow();
        outsideDivisionId = outsideDivision.getIdOrThrow();

        Employee currentEmployee = employeeRepository.save(createEmployee(ownDivisionId, USERNAME, "인가사용자"));
        selfEmployeeId = currentEmployee.getIdOrThrow();
        accountRepository.save(Account.create(selfEmployeeId, USERNAME, passwordEncoder.encode(PASSWORD)));

        sameDepartmentEmployeeId = employeeRepository.save(createEmployee(
                ownDivisionId,
                "same-department@abacus.co.kr",
                "같은부서직원"
        )).getIdOrThrow();
        childDepartmentEmployeeId = employeeRepository.save(createEmployee(
                childTeamId,
                "child-department@abacus.co.kr",
                "하위부서직원"
        )).getIdOrThrow();
        outsideDepartmentEmployeeId = employeeRepository.save(createEmployee(
                outsideDivisionId,
                "outside-department@abacus.co.kr",
                "외부부서직원"
        )).getIdOrThrow();
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
    @DisplayName("employee.read SELF 권한이 있으면 enum 조회 API에 접근할 수 있다")
    void should_allowEnumApis_whenGrantedSelfScope() throws Exception {
        grantEmployeeReadPermission(PermissionScope.SELF);
        MockHttpSession session = login();

        mockMvc.perform(get("/api/employees/positions").session(session))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("SELF 범위는 본인 직원 상세만 조회할 수 있다")
    void should_allowOnlySelfDetail_whenGrantedSelfScope() throws Exception {
        grantEmployeeReadPermission(PermissionScope.SELF);
        MockHttpSession session = login();

        mockMvc.perform(get("/api/employees/{id}", selfEmployeeId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(selfEmployeeId));

        mockMvc.perform(get("/api/employees/{id}", sameDepartmentEmployeeId).session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("OWN_DEPARTMENT 범위는 같은 부서만 조회할 수 있다")
    void should_allowOnlyOwnDepartmentDetail_whenGrantedOwnDepartmentScope() throws Exception {
        grantEmployeeReadPermission(PermissionScope.OWN_DEPARTMENT);
        MockHttpSession session = login();

        mockMvc.perform(get("/api/employees/{id}", sameDepartmentEmployeeId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(sameDepartmentEmployeeId));

        mockMvc.perform(get("/api/employees/{id}", childDepartmentEmployeeId).session(session))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/employees/{id}", outsideDepartmentEmployeeId).session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("OWN_DEPARTMENT_TREE 범위는 하위 부서를 포함해 조회할 수 있다")
    void should_allowDepartmentTreeDetail_whenGrantedOwnDepartmentTreeScope() throws Exception {
        grantEmployeeReadPermission(PermissionScope.OWN_DEPARTMENT_TREE);
        MockHttpSession session = login();

        mockMvc.perform(get("/api/employees/{id}", childDepartmentEmployeeId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(childDepartmentEmployeeId));

        mockMvc.perform(get("/api/employees/{id}", outsideDepartmentEmployeeId).session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("직원 상세 조회에서 대상이 없으면 404를 반환한다")
    void should_returnNotFound_whenEmployeeDoesNotExist() throws Exception {
        grantEmployeeReadPermission(PermissionScope.ALL);
        MockHttpSession session = login();

        mockMvc.perform(get("/api/employees/{id}", 99999L).session(session))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("SELF 범위 목록 조회는 본인만 반환한다")
    void should_returnOnlySelfInSearch_whenGrantedSelfScope() throws Exception {
        grantEmployeeReadPermission(PermissionScope.SELF);
        MockHttpSession session = login();

        mockMvc.perform(get("/api/employees")
                        .param("page", "0")
                        .param("size", "20")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].email", contains(USERNAME)));
    }

    @Test
    @DisplayName("OWN_DEPARTMENT 범위 목록 조회는 같은 부서 직원만 반환한다")
    void should_returnOnlyOwnDepartmentEmployeesInSearch_whenGrantedOwnDepartmentScope() throws Exception {
        grantEmployeeReadPermission(PermissionScope.OWN_DEPARTMENT);
        MockHttpSession session = login();

        mockMvc.perform(get("/api/employees")
                        .param("page", "0")
                        .param("size", "20")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].email", containsInAnyOrder(
                        USERNAME,
                        "same-department@abacus.co.kr"
                )));
    }

    @Test
    @DisplayName("OWN_DEPARTMENT_TREE 범위 목록 조회는 하위 부서를 포함한다")
    void should_returnDepartmentTreeEmployeesInSearch_whenGrantedOwnDepartmentTreeScope() throws Exception {
        grantEmployeeReadPermission(PermissionScope.OWN_DEPARTMENT_TREE);
        MockHttpSession session = login();

        mockMvc.perform(get("/api/employees")
                        .param("page", "0")
                        .param("size", "20")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].email", containsInAnyOrder(
                        USERNAME,
                        "same-department@abacus.co.kr",
                        "child-department@abacus.co.kr"
                )));
    }

    @Test
    @DisplayName("목록 조회의 부서 필터는 허용 범위와 교집합 처리한다")
    void should_intersectRequestedDepartmentsWithAllowedRange_whenSearchingEmployees() throws Exception {
        grantEmployeeReadPermission(PermissionScope.OWN_DEPARTMENT_TREE);
        MockHttpSession session = login();

        mockMvc.perform(get("/api/employees")
                        .param("page", "0")
                        .param("size", "20")
                        .param("departmentIds", String.valueOf(outsideDivisionId))
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @DisplayName("employee.read 권한이 없으면 직원 엑셀 다운로드는 403을 반환한다")
    void should_returnForbidden_whenDownloadingExcelWithoutEmployeeReadPermission() throws Exception {
        MockHttpSession session = login();

        mockMvc.perform(get("/api/employees/excel/download").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SELF 범위 엑셀 다운로드는 본인만 포함한다")
    void should_downloadOnlySelf_whenGrantedSelfScope() throws Exception {
        grantEmployeeReadPermission(PermissionScope.SELF);
        MockHttpSession session = login();

        byte[] content = mockMvc.perform(get("/api/employees/excel/download").session(session))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        assertThat(extractEmails(content)).containsExactly(USERNAME);
    }

    @Test
    @DisplayName("OWN_DEPARTMENT_TREE 범위 엑셀 다운로드는 허용 범위 직원만 포함한다")
    void should_downloadOnlyAllowedEmployees_whenGrantedOwnDepartmentTreeScope() throws Exception {
        grantEmployeeReadPermission(PermissionScope.OWN_DEPARTMENT_TREE);
        MockHttpSession session = login();

        byte[] content = mockMvc.perform(get("/api/employees/excel/download").session(session))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        assertThat(extractEmails(content)).containsExactlyInAnyOrder(
                USERNAME,
                "same-department@abacus.co.kr",
                "child-department@abacus.co.kr"
        );
        assertThat(extractEmails(content)).doesNotContain("outside-department@abacus.co.kr");
    }

    private void grantEmployeeReadPermission(PermissionScope... scopes) {
        Account account = accountRepository.findByUsername(new Email(USERNAME)).orElseThrow();
        Permission permission = permissionRepository.save(Permission.create(
                "employee.read",
                "직원 조회",
                "직원 조회 권한"
        ));
        PermissionGroup permissionGroup = permissionGroupRepository.save(PermissionGroup.create(
                "직원 조회 그룹",
                "직원 조회 권한 그룹",
                PermissionGroupType.CUSTOM
        ));

        accountGroupAssignmentRepository.save(AccountGroupAssignment.create(
                account.getIdOrThrow(),
                permissionGroup.getIdOrThrow()
        ));
        groupPermissionGrantRepository.saveAll(
                Set.of(scopes).stream()
                        .map(scope -> GroupPermissionGrant.create(
                                permissionGroup.getIdOrThrow(),
                                permission.getIdOrThrow(),
                                scope
                        ))
                        .toList()
        );
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

    private Set<String> extractEmails(byte[] content) throws Exception {
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(content))) {
            Set<String> emails = new LinkedHashSet<>();
            for (int rowIndex = 1; rowIndex <= workbook.getSheetAt(0).getLastRowNum(); rowIndex++) {
                Row row = workbook.getSheetAt(0).getRow(rowIndex);
                if (row == null || row.getCell(1) == null) {
                    continue;
                }
                emails.add(row.getCell(1).getStringCellValue());
            }
            return emails;
        }
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
