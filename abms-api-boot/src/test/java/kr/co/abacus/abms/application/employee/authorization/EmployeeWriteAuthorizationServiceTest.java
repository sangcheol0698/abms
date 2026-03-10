package kr.co.abacus.abms.application.employee.authorization;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import kr.co.abacus.abms.application.auth.outbound.AccountRepository;
import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.dto.EmployeeCreateCommand;
import kr.co.abacus.abms.application.employee.dto.EmployeeUpdateCommand;
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

@DisplayName("직원 쓰기 범위 권한 (EmployeeWriteAuthorizationService)")
class EmployeeWriteAuthorizationServiceTest extends IntegrationTestBase {

    private static final String PASSWORD = "Password123!";
    private static final String USERNAME = "employee-write-scope@abacus.co.kr";

    @Autowired
    private EmployeeWriteAuthorizationService employeeWriteAuthorizationService;

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
    private Long sameDepartmentEmployeeId;
    private Long childDepartmentEmployeeId;
    private Long siblingEmployeeId;

    @BeforeEach
    void setUpAccount() {
        Department division = departmentRepository.save(Department.create(
                "WRITE-DIVISION",
                "쓰기 본부",
                DepartmentType.DIVISION,
                null,
                null
        ));
        Department childTeam = departmentRepository.save(Department.create(
                "WRITE-TEAM",
                "쓰기 팀",
                DepartmentType.TEAM,
                null,
                division
        ));
        Department siblingDivision = departmentRepository.save(Department.create(
                "WRITE-SIBLING",
                "다른 본부",
                DepartmentType.DIVISION,
                null,
                null
        ));

        divisionId = division.getIdOrThrow();
        childTeamId = childTeam.getIdOrThrow();
        siblingDivisionId = siblingDivision.getIdOrThrow();

        Employee employee = employeeRepository.save(createEmployee(divisionId, USERNAME, "쓰기권한사용자"));
        selfEmployeeId = employee.getIdOrThrow();
        Account account = accountRepository.save(Account.create(
                selfEmployeeId,
                USERNAME,
                passwordEncoder.encode(PASSWORD)
        ));
        accountId = account.getIdOrThrow();

        sameDepartmentEmployeeId = employeeRepository.save(createEmployee(
                divisionId,
                "same-write@abacus.co.kr",
                "같은부서직원"
        )).getIdOrThrow();
        childDepartmentEmployeeId = employeeRepository.save(createEmployee(
                childTeamId,
                "child-write@abacus.co.kr",
                "하위부서직원"
        )).getIdOrThrow();
        siblingEmployeeId = employeeRepository.save(createEmployee(
                siblingDivisionId,
                "outside-write@abacus.co.kr",
                "다른부서직원"
        )).getIdOrThrow();
        flushAndClear();
    }

    @Test
    @DisplayName("ALL 범위가 있으면 전체 쓰기 허용으로 계산한다")
    void resolveScope_all() {
        grantEmployeeWritePermission(PermissionScope.ALL);

        EmployeeWriteScope scope = employeeWriteAuthorizationService.resolveScope(accountId);

        assertThat(scope.allAllowed()).isTrue();
        assertThat(scope.allowedDepartmentIds()).isEmpty();
        assertThat(scope.selfEmployeeId()).isNull();
    }

    @Test
    @DisplayName("OWN_DEPARTMENT 범위가 있으면 현재 부서만 쓰기 허용한다")
    void resolveScope_ownDepartment() {
        grantEmployeeWritePermission(PermissionScope.OWN_DEPARTMENT);

        EmployeeWriteScope scope = employeeWriteAuthorizationService.resolveScope(accountId);

        assertThat(scope.allowedDepartmentIds()).containsExactly(divisionId);
        assertThat(scope.canCreateIn(divisionId)).isTrue();
        assertThat(scope.canCreateIn(siblingDivisionId)).isFalse();
    }

    @Test
    @DisplayName("OWN_DEPARTMENT_TREE 범위가 있으면 현재 부서와 하위 부서를 모두 허용한다")
    void resolveScope_ownDepartmentTree() {
        grantEmployeeWritePermission(PermissionScope.OWN_DEPARTMENT_TREE);

        EmployeeWriteScope scope = employeeWriteAuthorizationService.resolveScope(accountId);

        assertThat(scope.allowedDepartmentIds()).containsExactlyInAnyOrder(divisionId, childTeamId);
        assertThat(scope.allowedDepartmentIds()).doesNotContain(siblingDivisionId);
    }

    @Test
    @DisplayName("SELF 범위는 본인 프로필 수정만 허용한다")
    void resolveScope_self() {
        grantEmployeeWritePermission(PermissionScope.SELF);

        EmployeeWriteScope scope = employeeWriteAuthorizationService.resolveScope(accountId);

        assertThat(scope.allowedDepartmentIds()).isEmpty();
        assertThat(scope.selfEmployeeId()).isEqualTo(selfEmployeeId);
        assertThat(scope.canCreateIn(divisionId)).isFalse();
        assertThat(scope.canManageTarget(divisionId)).isFalse();
        assertThat(scope.canUpdateOwnProfile(selfEmployeeId)).isTrue();
    }

    @Test
    @DisplayName("SELF 범위는 본인 이름, 생년월일, 아바타 수정만 허용한다")
    void assertCanUpdate_selfProfileOnly() {
        grantEmployeeWritePermission(PermissionScope.SELF);

        assertThatCode(() -> employeeWriteAuthorizationService.assertCanUpdate(
                accountId,
                selfEmployeeId,
                allowedSelfUpdateCommand()
        )).doesNotThrowAnyException();

        assertThatThrownBy(() -> employeeWriteAuthorizationService.assertCanUpdate(
                accountId,
                selfEmployeeId,
                selfUpdateCommand(builder -> builder.email("changed@abacus.co.kr"))
        )).isInstanceOf(AccessDeniedException.class);

        assertThatThrownBy(() -> employeeWriteAuthorizationService.assertCanUpdate(
                accountId,
                selfEmployeeId,
                selfUpdateCommand(builder -> builder.memo("메모 변경"))
        )).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("SELF 범위는 본인 외 수정과 생성, 상태 변경을 허용하지 않는다")
    void assertSelfScope_deniesCreateAndManage() {
        grantEmployeeWritePermission(PermissionScope.SELF);

        assertThatThrownBy(() -> employeeWriteAuthorizationService.assertCanCreate(accountId, divisionId))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> employeeWriteAuthorizationService.assertCanUpdate(
                accountId,
                sameDepartmentEmployeeId,
                allowedSelfUpdateCommand()
        )).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> employeeWriteAuthorizationService.assertCanManage(accountId, selfEmployeeId))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> employeeWriteAuthorizationService.assertCanUpload(
                accountId,
                List.of(createCommand(divisionId, "upload@abacus.co.kr"))
        )).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("OWN_DEPARTMENT_TREE 범위는 하위 부서 직원 수정은 허용하고 범위 밖 직원 수정은 막는다")
    void assertCanUpdate_departmentTree() {
        grantEmployeeWritePermission(PermissionScope.OWN_DEPARTMENT_TREE);

        assertThatCode(() -> employeeWriteAuthorizationService.assertCanUpdate(
                accountId,
                childDepartmentEmployeeId,
                createUpdateCommand(childTeamId, "child-write@abacus.co.kr", "하위부서직원")
        )).doesNotThrowAnyException();

        assertThatThrownBy(() -> employeeWriteAuthorizationService.assertCanUpdate(
                accountId,
                siblingEmployeeId,
                createUpdateCommand(siblingDivisionId, "outside-write@abacus.co.kr", "다른부서직원")
        )).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("OWN_DEPARTMENT 범위는 범위 밖 부서로 이동시키는 수정을 막는다")
    void assertCanUpdate_deniesMovingOutsideAllowedDepartments() {
        grantEmployeeWritePermission(PermissionScope.OWN_DEPARTMENT);

        assertThatThrownBy(() -> employeeWriteAuthorizationService.assertCanUpdate(
                accountId,
                sameDepartmentEmployeeId,
                createUpdateCommand(siblingDivisionId, "same-write@abacus.co.kr", "같은부서직원")
        )).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("직원 컨텍스트를 찾을 수 없으면 쓰기 권한을 부여하지 않는다")
    void resolveScope_missingEmployeeContext() {
        grantEmployeeWritePermission(PermissionScope.OWN_DEPARTMENT);
        Employee employee = employeeRepository.findByIdAndDeletedFalse(selfEmployeeId).orElseThrow();
        employee.softDelete(1L);
        flushAndClear();

        EmployeeWriteScope scope = employeeWriteAuthorizationService.resolveScope(accountId);

        assertThat(scope).isEqualTo(EmployeeWriteScope.none());
    }

    @Test
    @DisplayName("CURRENT_PARTICIPATION만 있으면 쓰기 범위를 넓히지 않는다")
    void resolveScope_currentParticipationDoesNotGrantWriteRange() {
        grantEmployeeWritePermission(PermissionScope.CURRENT_PARTICIPATION);

        EmployeeWriteScope scope = employeeWriteAuthorizationService.resolveScope(accountId);

        assertThat(scope).isEqualTo(EmployeeWriteScope.none());
    }

    private void grantEmployeeWritePermission(PermissionScope... scopes) {
        Permission permission = permissionRepository.save(Permission.create(
                EmployeeWriteAuthorizationService.EMPLOYEE_WRITE_PERMISSION_CODE,
                "직원 쓰기",
                "직원 쓰기 권한"
        ));
        PermissionGroup permissionGroup = permissionGroupRepository.save(PermissionGroup.create(
                "직원 쓰기 범위 그룹",
                "직원 쓰기 범위 권한 그룹",
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

    private EmployeeUpdateCommand allowedSelfUpdateCommand() {
        return selfUpdateCommand(builder -> builder
                .name("새이름")
                .birthDate(LocalDate.of(1991, 6, 1))
                .avatar(EmployeeAvatar.AQUA_SPLASH));
    }

    private EmployeeUpdateCommand selfUpdateCommand(
            java.util.function.UnaryOperator<EmployeeUpdateCommand.EmployeeUpdateCommandBuilder> mutator
    ) {
        Employee target = employeeRepository.findByIdAndDeletedFalse(selfEmployeeId).orElseThrow();
        return mutator.apply(EmployeeUpdateCommand.builder()
                        .departmentId(target.getDepartmentId())
                        .email(target.getEmail().address())
                        .name(target.getName())
                        .joinDate(target.getJoinDate())
                        .birthDate(target.getBirthDate())
                        .position(target.getPosition())
                        .type(target.getType())
                        .grade(target.getGrade())
                        .avatar(target.getAvatar())
                        .memo(target.getMemo()))
                .build();
    }

    private EmployeeUpdateCommand createUpdateCommand(Long departmentId, String email, String name) {
        return EmployeeUpdateCommand.builder()
                .departmentId(departmentId)
                .email(email)
                .name(name)
                .joinDate(LocalDate.of(2024, 1, 1))
                .birthDate(LocalDate.of(1990, 5, 20))
                .position(EmployeePosition.ASSOCIATE)
                .type(EmployeeType.FULL_TIME)
                .grade(EmployeeGrade.JUNIOR)
                .avatar(EmployeeAvatar.SKY_GLOW)
                .memo(null)
                .build();
    }

    private EmployeeCreateCommand createCommand(Long departmentId, String email) {
        return EmployeeCreateCommand.builder()
                .departmentId(departmentId)
                .email(email)
                .name("업로드직원")
                .joinDate(LocalDate.of(2024, 1, 1))
                .birthDate(LocalDate.of(1990, 5, 20))
                .position(EmployeePosition.ASSOCIATE)
                .type(EmployeeType.FULL_TIME)
                .grade(EmployeeGrade.JUNIOR)
                .avatar(EmployeeAvatar.SKY_GLOW)
                .memo(null)
                .build();
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
