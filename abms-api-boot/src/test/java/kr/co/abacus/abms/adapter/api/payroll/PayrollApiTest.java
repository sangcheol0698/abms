package kr.co.abacus.abms.adapter.api.payroll;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;

import kr.co.abacus.abms.adapter.api.payroll.dto.EmployeePayrollChangeRequest;
import kr.co.abacus.abms.adapter.api.payroll.dto.PayrollCreateRequest;
import kr.co.abacus.abms.application.auth.outbound.AccountRepository;
import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.payroll.outbound.PayrollRepository;
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
import kr.co.abacus.abms.domain.payroll.Payroll;
import kr.co.abacus.abms.domain.permission.Permission;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroup;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroupType;
import kr.co.abacus.abms.domain.shared.Money;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

@DisplayName("급여 API (PayrollApi)")
class PayrollApiTest extends ApiIntegrationTestBase {

    private static final String USERNAME = "payroll-api-user@abacus.co.kr";
    private static final String PASSWORD = "Password123!";

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PayrollRepository payrollRepository;

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
    private Long outsideDivisionId;
    private Long selfEmployeeId;
    private Long sameDepartmentEmployeeId;
    private Long outsideDepartmentEmployeeId;

    @BeforeEach
    void setUpAccount() {
        Department ownDivision = departmentRepository.save(Department.create(
                "PAYROLL-AUTH-DIVISION",
                "급여 인가 본부",
                DepartmentType.DIVISION,
                null,
                null
        ));
        Department outsideDivision = departmentRepository.save(Department.create(
                "PAYROLL-AUTH-OUTSIDE",
                "급여 외부 본부",
                DepartmentType.DIVISION,
                null,
                null
        ));

        ownDivisionId = ownDivision.getIdOrThrow();
        outsideDivisionId = outsideDivision.getIdOrThrow();

        Employee currentEmployee = employeeRepository.save(createEmployee(ownDivisionId, USERNAME, "급여사용자"));
        selfEmployeeId = currentEmployee.getIdOrThrow();
        accountRepository.save(Account.create(selfEmployeeId, USERNAME, passwordEncoder.encode(PASSWORD)));

        sameDepartmentEmployeeId = employeeRepository.save(createEmployee(
                ownDivisionId,
                "payroll-same@abacus.co.kr",
                "같은부서직원"
        )).getIdOrThrow();
        outsideDepartmentEmployeeId = employeeRepository.save(createEmployee(
                outsideDivisionId,
                "payroll-outside@abacus.co.kr",
                "외부부서직원"
        )).getIdOrThrow();
        flushAndClear();
    }

    @Test
    @DisplayName("직원 하위 경로로 현재 연봉과 이력을 조회한다")
    void getPayrolls() throws Exception {
        grantPermission("employee.read", PermissionScope.OWN_DEPARTMENT);
        grantPermission("employee.write", PermissionScope.OWN_DEPARTMENT);
        payrollRepository.save(Payroll.create(sameDepartmentEmployeeId, Money.wons(30_000_000L), LocalDate.of(2024, 1, 1)));
        Payroll current = Payroll.create(sameDepartmentEmployeeId, Money.wons(36_000_000L), LocalDate.of(2025, 1, 1));
        Payroll previous = payrollRepository.findCurrentSalaryByEmployeeId(sameDepartmentEmployeeId).orElseThrow();
        previous.close(LocalDate.of(2024, 12, 31));
        payrollRepository.save(previous);
        payrollRepository.save(current);
        flushAndClear();

        MockHttpSession session = login();

        mockMvc.perform(get("/api/employees/{employeeId}/payroll", sameDepartmentEmployeeId).session(session))
                .andDo(document("payroll/current",
                        pathParameters(
                                parameterWithName("employeeId").description("현재 연봉을 조회할 직원 ID")
                        ),
                        responseBody()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(sameDepartmentEmployeeId))
                .andExpect(jsonPath("$.annualSalary").value(36_000_000L))
                .andExpect(jsonPath("$.monthlySalary").value(3_000_000L))
                .andExpect(jsonPath("$.status").value("CURRENT"));

        mockMvc.perform(get("/api/employees/{employeeId}/payroll-history", sameDepartmentEmployeeId).session(session))
                .andDo(document("payroll/history",
                        pathParameters(
                                parameterWithName("employeeId").description("연봉 이력을 조회할 직원 ID")
                        ),
                        responseBody()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].annualSalary").value(36_000_000L))
                .andExpect(jsonPath("$[0].status").value("CURRENT"))
                .andExpect(jsonPath("$[1].annualSalary").value(30_000_000L))
                .andExpect(jsonPath("$[1].status").value("ENDED"));
    }

    @Test
    @DisplayName("SELF 범위는 본인 연봉 조회는 가능하지만 변경은 불가하다")
    void selfScopeCanReadButCannotWrite() throws Exception {
        grantPermission("employee.read", PermissionScope.SELF);
        grantPermission("employee.write", PermissionScope.SELF);
        payrollRepository.save(Payroll.create(selfEmployeeId, Money.wons(30_000_000L), LocalDate.of(2025, 1, 1)));
        flushAndClear();

        MockHttpSession session = login();

        mockMvc.perform(get("/api/employees/{employeeId}/payroll", selfEmployeeId).session(session))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/employees/{employeeId}/payroll-history", selfEmployeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EmployeePayrollChangeRequest(
                                BigDecimal.valueOf(40_000_000L),
                                LocalDate.of(2025, 2, 1))))
                        .session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("employee.read 권한이 없으면 연봉 조회는 403을 반환한다")
    void forbidReadWithoutEmployeeReadPermission() throws Exception {
        MockHttpSession session = login();

        mockMvc.perform(get("/api/employees/{employeeId}/payroll", sameDepartmentEmployeeId).session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("employee.write 권한이 없으면 연봉 변경은 403을 반환한다")
    void forbidWriteWithoutEmployeeWritePermission() throws Exception {
        grantPermission("employee.read", PermissionScope.OWN_DEPARTMENT);
        MockHttpSession session = login();

        mockMvc.perform(post("/api/employees/{employeeId}/payroll-history", sameDepartmentEmployeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EmployeePayrollChangeRequest(
                                BigDecimal.valueOf(40_000_000L),
                                LocalDate.of(2025, 2, 1))))
                        .session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("기존 salary-history 경로는 호환용 alias로 유지된다")
    void legacyAlias() throws Exception {
        grantPermission("employee.write", PermissionScope.OWN_DEPARTMENT);
        MockHttpSession session = login();
        String responseJson = objectMapper.writeValueAsString(
                new PayrollCreateRequest(sameDepartmentEmployeeId, BigDecimal.valueOf(30_000_000), LocalDate.of(2025, 1, 1))
        );

        mockMvc.perform(post("/api/salary-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(responseJson)
                        .session(session))
                .andDo(document("payroll/legacy-change",
                        requestBody()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("직원 하위 경로로 연봉을 변경한다")
    void changeSalary() throws Exception {
        grantPermission("employee.write", PermissionScope.OWN_DEPARTMENT);
        MockHttpSession session = login();

        mockMvc.perform(post("/api/employees/{employeeId}/payroll-history", sameDepartmentEmployeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EmployeePayrollChangeRequest(
                                BigDecimal.valueOf(30_000_000L),
                                LocalDate.of(2025, 1, 1))))
                        .session(session))
                .andDo(document("payroll/change",
                        pathParameters(
                                parameterWithName("employeeId").description("연봉을 변경할 직원 ID")
                        ),
                        requestBody()))
                .andExpect(status().isNoContent());

        Payroll currentPayroll = payrollRepository.findCurrentSalaryByEmployeeId(sameDepartmentEmployeeId).orElseThrow();

        assertThat(currentPayroll.getEmployeeId()).isEqualTo(sameDepartmentEmployeeId);
        assertThat(currentPayroll.getAnnualSalary()).isEqualTo(Money.wons(30_000_000L));
        assertThat(currentPayroll.getPeriod().startDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(currentPayroll.getPeriod().endDate()).isNull();
    }

    @Test
    @DisplayName("OWN_DEPARTMENT 범위는 범위 밖 직원 연봉 조회를 막는다")
    void forbidOutsideDepartmentRead() throws Exception {
        grantPermission("employee.read", PermissionScope.OWN_DEPARTMENT);
        MockHttpSession session = login();

        mockMvc.perform(get("/api/employees/{employeeId}/payroll", outsideDepartmentEmployeeId).session(session))
                .andExpect(status().isForbidden());
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
                code + "-group",
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
        String body = objectMapper.writeValueAsString(java.util.Map.of(
                "username", USERNAME,
                "password", PASSWORD
        ));

        var result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        return (MockHttpSession) result.getRequest().getSession(false);
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
                null);
    }

}
