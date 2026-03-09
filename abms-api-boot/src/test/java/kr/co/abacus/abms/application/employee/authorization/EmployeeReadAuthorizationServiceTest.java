package kr.co.abacus.abms.application.employee.authorization;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

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
import kr.co.abacus.abms.support.IntegrationTestBase;

@DisplayName("직원 조회 범위 권한 (EmployeeReadAuthorizationService)")
class EmployeeReadAuthorizationServiceTest extends IntegrationTestBase {

    private static final String PASSWORD = "Password123!";
    private static final String USERNAME = "employee-read-scope@abacus.co.kr";

    @Autowired
    private EmployeeReadAuthorizationService employeeReadAuthorizationService;

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

    private Long accountId;
    private Long divisionId;
    private Long childTeamId;
    private Long siblingDivisionId;
    private Long selfEmployeeId;

    @BeforeEach
    void setUpAccount() {
        Department division = departmentRepository.save(Department.create(
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
                division
        ));
        Department siblingDivision = departmentRepository.save(Department.create(
                "AUTH-SIBLING",
                "다른 본부",
                DepartmentType.DIVISION,
                null,
                null
        ));

        divisionId = division.getIdOrThrow();
        childTeamId = childTeam.getIdOrThrow();
        siblingDivisionId = siblingDivision.getIdOrThrow();

        Employee employee = employeeRepository.save(createEmployee(divisionId, USERNAME, "범위권한사용자"));
        selfEmployeeId = employee.getIdOrThrow();
        Account account = accountRepository.save(Account.create(
                selfEmployeeId,
                USERNAME,
                passwordEncoder.encode(PASSWORD)
        ));
        accountId = account.getIdOrThrow();
        flushAndClear();
    }

    @Test
    @DisplayName("ALL 범위가 있으면 전체 허용으로 계산한다")
    void resolveScope_all() {
        grantEmployeeReadPermission(PermissionScope.ALL);

        EmployeeReadScope scope = employeeReadAuthorizationService.resolveScope(accountId);

        assertThat(scope.allAllowed()).isTrue();
        assertThat(scope.allowedDepartmentIds()).isEmpty();
        assertThat(scope.selfEmployeeId()).isNull();
    }

    @Test
    @DisplayName("OWN_DEPARTMENT 범위가 있으면 현재 부서만 허용한다")
    void resolveScope_ownDepartment() {
        grantEmployeeReadPermission(PermissionScope.OWN_DEPARTMENT);

        EmployeeReadScope scope = employeeReadAuthorizationService.resolveScope(accountId);

        assertThat(scope.allAllowed()).isFalse();
        assertThat(scope.allowedDepartmentIds()).containsExactly(divisionId);
        assertThat(scope.selfEmployeeId()).isNull();
    }

    @Test
    @DisplayName("OWN_DEPARTMENT_TREE 범위가 있으면 현재 부서와 하위 부서를 모두 허용한다")
    void resolveScope_ownDepartmentTree() {
        grantEmployeeReadPermission(PermissionScope.OWN_DEPARTMENT_TREE);

        EmployeeReadScope scope = employeeReadAuthorizationService.resolveScope(accountId);

        assertThat(scope.allowedDepartmentIds()).containsExactlyInAnyOrder(divisionId, childTeamId);
        assertThat(scope.allowedDepartmentIds()).doesNotContain(siblingDivisionId);
    }

    @Test
    @DisplayName("SELF 범위가 있으면 본인 직원 정보만 허용한다")
    void resolveScope_self() {
        grantEmployeeReadPermission(PermissionScope.SELF);

        EmployeeReadScope scope = employeeReadAuthorizationService.resolveScope(accountId);

        assertThat(scope.allowedDepartmentIds()).isEmpty();
        assertThat(scope.selfEmployeeId()).isEqualTo(selfEmployeeId);
        assertThat(scope.selfDepartmentId()).isEqualTo(divisionId);
    }

    @Test
    @DisplayName("여러 범위가 함께 있으면 부서 범위와 본인 범위를 함께 정규화한다")
    void resolveScope_multipleScopes() {
        grantEmployeeReadPermission(PermissionScope.OWN_DEPARTMENT_TREE, PermissionScope.SELF);

        EmployeeReadScope scope = employeeReadAuthorizationService.resolveScope(accountId);

        assertThat(scope.allowedDepartmentIds()).containsExactlyInAnyOrder(divisionId, childTeamId);
        assertThat(scope.selfEmployeeId()).isEqualTo(selfEmployeeId);
        assertThat(scope.canRead(selfEmployeeId, divisionId)).isTrue();
        assertThat(scope.canRead(9999L, siblingDivisionId)).isFalse();
    }

    @Test
    @DisplayName("직원 컨텍스트를 찾을 수 없으면 접근 불가 범위를 반환한다")
    void resolveScope_missingEmployeeContext() {
        grantEmployeeReadPermission(PermissionScope.OWN_DEPARTMENT);
        Employee employee = employeeRepository.findByIdAndDeletedFalse(selfEmployeeId).orElseThrow();
        employee.softDelete(1L);
        flushAndClear();

        EmployeeReadScope scope = employeeReadAuthorizationService.resolveScope(accountId);

        assertThat(scope).isEqualTo(EmployeeReadScope.none());
    }

    @Test
    @DisplayName("CURRENT_PARTICIPATION만 있으면 접근 범위를 넓히지 않는다")
    void resolveScope_currentParticipationDoesNotGrantReadRange() {
        grantEmployeeReadPermission(PermissionScope.CURRENT_PARTICIPATION);

        EmployeeReadScope scope = employeeReadAuthorizationService.resolveScope(accountId);

        assertThat(scope).isEqualTo(EmployeeReadScope.none());
    }

    @Test
    @DisplayName("요청 부서 필터를 적용할 때 허용 범위와 교집합 처리한다")
    void limitToRequestedDepartments_intersectsRequestedDepartments() {
        grantEmployeeReadPermission(PermissionScope.OWN_DEPARTMENT_TREE, PermissionScope.SELF);

        EmployeeReadScope scope = employeeReadAuthorizationService.resolveScope(accountId)
                .limitToRequestedDepartments(java.util.List.of(childTeamId));

        assertThat(scope.allowedDepartmentIds()).containsExactly(childTeamId);
        assertThat(scope.selfEmployeeId()).isNull();
    }

    private void grantEmployeeReadPermission(PermissionScope... scopes) {
        Permission permission = permissionRepository.save(Permission.create(
                EmployeeReadAuthorizationService.EMPLOYEE_READ_PERMISSION_CODE,
                "직원 조회",
                "직원 조회 권한"
        ));
        PermissionGroup permissionGroup = permissionGroupRepository.save(PermissionGroup.create(
                "직원 조회 범위 그룹",
                "직원 조회 범위 권한 그룹",
                PermissionGroupType.CUSTOM
        ));

        accountGroupAssignmentRepository.save(AccountGroupAssignment.create(accountId, permissionGroup.getIdOrThrow()));
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
