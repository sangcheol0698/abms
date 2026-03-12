package kr.co.abacus.abms.application.permission.inbound;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import kr.co.abacus.abms.application.auth.outbound.AccountRepository;
import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.permission.dto.GrantedPermissionDetail;
import kr.co.abacus.abms.application.permission.dto.PermissionDetail;
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

@DisplayName("권한 조회 (PermissionFinder)")
class PermissionFinderTest extends IntegrationTestBase {

    @Autowired
    private PermissionFinder permissionFinder;

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

    @Test
    @DisplayName("계정에 연결된 권한과 범위를 조회한다")
    void findPermissions() {
        Long accountId = createAccount("permission-user-1@abacus.co.kr");
        Permission permission = permissionRepository.save(Permission.create(
                "employee.read",
                "직원 조회",
                "직원 목록과 상세를 조회할 수 있다."
        ));
        PermissionGroup permissionGroup = permissionGroupRepository.save(PermissionGroup.create(
                "직원 조회 그룹",
                "직원 조회 권한을 가진 그룹이다.",
                PermissionGroupType.CUSTOM
        ));
        accountGroupAssignmentRepository.save(AccountGroupAssignment.create(accountId, permissionGroup.getIdOrThrow()));
        groupPermissionGrantRepository.save(GroupPermissionGrant.create(
                permissionGroup.getIdOrThrow(),
                permission.getIdOrThrow(),
                PermissionScope.OWN_DEPARTMENT
        ));
        flushAndClear();

        PermissionDetail result = permissionFinder.findPermissions(accountId);

        assertThat(result.accountId()).isEqualTo(accountId);
        assertThat(result.permissions()).containsExactly(
                new GrantedPermissionDetail("employee.read", Set.of(PermissionScope.OWN_DEPARTMENT))
        );
    }

    @Test
    @DisplayName("여러 권한 그룹의 동일 권한은 범위 집합으로 병합한다")
    void mergeScopesFromMultipleGroups() {
        Long accountId = createAccount("permission-user-2@abacus.co.kr");
        Permission permission = permissionRepository.save(Permission.create(
                "project.read",
                "프로젝트 조회",
                "프로젝트를 조회할 수 있다."
        ));
        PermissionGroup teamGroup = permissionGroupRepository.save(PermissionGroup.create(
                "부서 조회 그룹",
                "부서 기준 프로젝트 조회 권한",
                PermissionGroupType.CUSTOM
        ));
        PermissionGroup selfGroup = permissionGroupRepository.save(PermissionGroup.create(
                "본인 조회 그룹",
                "본인 기준 프로젝트 조회 권한",
                PermissionGroupType.CUSTOM
        ));
        accountGroupAssignmentRepository.saveAll(Set.of(
                AccountGroupAssignment.create(accountId, teamGroup.getIdOrThrow()),
                AccountGroupAssignment.create(accountId, selfGroup.getIdOrThrow())
        ));
        groupPermissionGrantRepository.saveAll(Set.of(
                GroupPermissionGrant.create(teamGroup.getIdOrThrow(), permission.getIdOrThrow(),
                        PermissionScope.OWN_DEPARTMENT),
                GroupPermissionGrant.create(selfGroup.getIdOrThrow(), permission.getIdOrThrow(), PermissionScope.SELF)
        ));
        flushAndClear();

        PermissionDetail result = permissionFinder.findPermissions(accountId);

        assertThat(result.permissions()).containsExactly(
                new GrantedPermissionDetail(
                        "project.read",
                        Set.of(PermissionScope.OWN_DEPARTMENT, PermissionScope.SELF)
                )
        );
    }

    @Test
    @DisplayName("soft delete 된 권한 그룹과 grant, 권한, 연결은 계산에서 제외한다")
    void excludeSoftDeletedRecords() {
        Long accountId = createAccount("permission-user-3@abacus.co.kr");
        Permission activePermission = permissionRepository.save(Permission.create(
                "employee.read",
                "직원 조회",
                "직원 조회"
        ));
        Permission deletedPermission = permissionRepository.save(Permission.create(
                "employee.update",
                "직원 수정",
                "직원 수정"
        ));

        PermissionGroup activeGroup = permissionGroupRepository.save(PermissionGroup.create(
                "활성 그룹",
                "활성 권한 그룹",
                PermissionGroupType.CUSTOM
        ));
        PermissionGroup deletedGroup = permissionGroupRepository.save(PermissionGroup.create(
                "삭제 그룹",
                "삭제된 권한 그룹",
                PermissionGroupType.CUSTOM
        ));

        AccountGroupAssignment activeAssignment =
                accountGroupAssignmentRepository.save(AccountGroupAssignment.create(accountId, activeGroup.getIdOrThrow()));
        AccountGroupAssignment deletedAssignment =
                accountGroupAssignmentRepository.save(AccountGroupAssignment.create(accountId, deletedGroup.getIdOrThrow()));

        GroupPermissionGrant activeGrant = groupPermissionGrantRepository.save(GroupPermissionGrant.create(
                activeGroup.getIdOrThrow(),
                activePermission.getIdOrThrow(),
                PermissionScope.ALL
        ));
        GroupPermissionGrant deletedGrant = groupPermissionGrantRepository.save(GroupPermissionGrant.create(
                activeGroup.getIdOrThrow(),
                deletedPermission.getIdOrThrow(),
                PermissionScope.SELF
        ));
        GroupPermissionGrant groupedDeletedGrant = groupPermissionGrantRepository.save(GroupPermissionGrant.create(
                deletedGroup.getIdOrThrow(),
                activePermission.getIdOrThrow(),
                PermissionScope.OWN_DEPARTMENT
        ));

        deletedPermission.softDelete(1L);
        deletedGroup.softDelete(1L);
        deletedAssignment.softDelete(1L);
        deletedGrant.softDelete(1L);
        groupedDeletedGrant.softDelete(1L);
        flushAndClear();

        PermissionDetail result = permissionFinder.findPermissions(accountId);

        assertThat(result.permissions()).containsExactly(
                new GrantedPermissionDetail("employee.read", Set.of(PermissionScope.ALL))
        );
    }

    @Test
    @DisplayName("soft delete 된 권한은 결과에서 제외한다")
    void excludeDeletedPermission() {
        Long accountId = createAccount("permission-user-5@abacus.co.kr");
        Permission deletedPermission = permissionRepository.save(Permission.create(
                "employee.delete",
                "직원 삭제",
                "직원 삭제"
        ));
        PermissionGroup permissionGroup = permissionGroupRepository.save(PermissionGroup.create(
                "직원 삭제 그룹",
                "직원 삭제 그룹",
                PermissionGroupType.CUSTOM
        ));
        accountGroupAssignmentRepository.save(AccountGroupAssignment.create(accountId, permissionGroup.getIdOrThrow()));
        groupPermissionGrantRepository.save(GroupPermissionGrant.create(
                permissionGroup.getIdOrThrow(),
                deletedPermission.getIdOrThrow(),
                PermissionScope.ALL
        ));
        deletedPermission.softDelete(1L);
        flushAndClear();

        PermissionDetail result = permissionFinder.findPermissions(accountId);

        assertThat(result.permissions()).isEmpty();
    }

    @Test
    @DisplayName("soft delete 된 권한 그룹은 결과에서 제외한다")
    void excludeDeletedPermissionGroup() {
        Long accountId = createAccount("permission-user-6@abacus.co.kr");
        Permission permission = permissionRepository.save(Permission.create(
                "employee.update",
                "직원 수정",
                "직원 수정"
        ));
        PermissionGroup deletedGroup = permissionGroupRepository.save(PermissionGroup.create(
                "삭제 그룹",
                "삭제 그룹",
                PermissionGroupType.CUSTOM
        ));
        accountGroupAssignmentRepository.save(AccountGroupAssignment.create(accountId, deletedGroup.getIdOrThrow()));
        groupPermissionGrantRepository.save(GroupPermissionGrant.create(
                deletedGroup.getIdOrThrow(),
                permission.getIdOrThrow(),
                PermissionScope.ALL
        ));
        deletedGroup.softDelete(1L);
        flushAndClear();

        PermissionDetail result = permissionFinder.findPermissions(accountId);

        assertThat(result.permissions()).isEmpty();
    }

    @Test
    @DisplayName("soft delete 된 계정-그룹 연결은 결과에서 제외한다")
    void excludeDeletedAssignment() {
        Long accountId = createAccount("permission-user-7@abacus.co.kr");
        Permission permission = permissionRepository.save(Permission.create(
                "project.update",
                "프로젝트 수정",
                "프로젝트 수정"
        ));
        PermissionGroup permissionGroup = permissionGroupRepository.save(PermissionGroup.create(
                "프로젝트 수정 그룹",
                "프로젝트 수정 그룹",
                PermissionGroupType.CUSTOM
        ));
        AccountGroupAssignment assignment = accountGroupAssignmentRepository.save(
                AccountGroupAssignment.create(accountId, permissionGroup.getIdOrThrow())
        );
        groupPermissionGrantRepository.save(GroupPermissionGrant.create(
                permissionGroup.getIdOrThrow(),
                permission.getIdOrThrow(),
                PermissionScope.ALL
        ));
        assignment.softDelete(1L);
        flushAndClear();

        PermissionDetail result = permissionFinder.findPermissions(accountId);

        assertThat(result.permissions()).isEmpty();
    }

    @Test
    @DisplayName("soft delete 된 grant는 결과에서 제외한다")
    void excludeDeletedGrant() {
        Long accountId = createAccount("permission-user-8@abacus.co.kr");
        Permission permission = permissionRepository.save(Permission.create(
                "project.delete",
                "프로젝트 삭제",
                "프로젝트 삭제"
        ));
        PermissionGroup permissionGroup = permissionGroupRepository.save(PermissionGroup.create(
                "프로젝트 삭제 그룹",
                "프로젝트 삭제 그룹",
                PermissionGroupType.CUSTOM
        ));
        accountGroupAssignmentRepository.save(AccountGroupAssignment.create(accountId, permissionGroup.getIdOrThrow()));
        GroupPermissionGrant grant = groupPermissionGrantRepository.save(GroupPermissionGrant.create(
                permissionGroup.getIdOrThrow(),
                permission.getIdOrThrow(),
                PermissionScope.ALL
        ));
        grant.softDelete(1L);
        flushAndClear();

        PermissionDetail result = permissionFinder.findPermissions(accountId);

        assertThat(result.permissions()).isEmpty();
    }

    @Test
    @DisplayName("권한 코드는 오름차순으로 정렬된다")
    void sortPermissionsByCode() {
        Long accountId = createAccount("permission-user-9@abacus.co.kr");
        Permission zPermission = permissionRepository.save(Permission.create(
                "z.permission",
                "마지막 권한",
                "마지막 권한"
        ));
        Permission aPermission = permissionRepository.save(Permission.create(
                "a.permission",
                "처음 권한",
                "처음 권한"
        ));
        PermissionGroup permissionGroup = permissionGroupRepository.save(PermissionGroup.create(
                "정렬 그룹",
                "정렬 그룹",
                PermissionGroupType.CUSTOM
        ));
        accountGroupAssignmentRepository.save(AccountGroupAssignment.create(accountId, permissionGroup.getIdOrThrow()));
        groupPermissionGrantRepository.saveAll(Set.of(
                GroupPermissionGrant.create(permissionGroup.getIdOrThrow(), zPermission.getIdOrThrow(), PermissionScope.ALL),
                GroupPermissionGrant.create(permissionGroup.getIdOrThrow(), aPermission.getIdOrThrow(), PermissionScope.ALL)
        ));
        flushAndClear();

        PermissionDetail result = permissionFinder.findPermissions(accountId);

        assertThat(result.permissions())
                .extracting(GrantedPermissionDetail::code)
                .containsExactly("a.permission", "z.permission");
    }

    @Test
    @DisplayName("권한 범위는 order 기준으로 정렬된다")
    void sortScopesByOrder() {
        Long accountId = createAccount("permission-user-10@abacus.co.kr");
        Permission permission = permissionRepository.save(Permission.create(
                "employee.manage",
                "직원 관리",
                "직원 관리"
        ));
        PermissionGroup permissionGroup = permissionGroupRepository.save(PermissionGroup.create(
                "직원 관리 그룹",
                "직원 관리 그룹",
                PermissionGroupType.CUSTOM
        ));
        accountGroupAssignmentRepository.save(AccountGroupAssignment.create(accountId, permissionGroup.getIdOrThrow()));
        groupPermissionGrantRepository.saveAll(Set.of(
                GroupPermissionGrant.create(permissionGroup.getIdOrThrow(), permission.getIdOrThrow(), PermissionScope.SELF),
                GroupPermissionGrant.create(permissionGroup.getIdOrThrow(), permission.getIdOrThrow(),
                        PermissionScope.OWN_DEPARTMENT),
                GroupPermissionGrant.create(permissionGroup.getIdOrThrow(), permission.getIdOrThrow(), PermissionScope.ALL)
        ));
        flushAndClear();

        PermissionDetail result = permissionFinder.findPermissions(accountId);

        assertThat(result.permissions()).containsExactly(
                new GrantedPermissionDetail(
                        "employee.manage",
                        Set.of(PermissionScope.ALL, PermissionScope.OWN_DEPARTMENT, PermissionScope.SELF)
                )
        );
    }

    @Test
    @DisplayName("동일 권한과 동일 범위의 중복 grant는 하나의 범위로 반환한다")
    void deduplicateSameScope() {
        Long accountId = createAccount("permission-user-11@abacus.co.kr");
        Permission permission = permissionRepository.save(Permission.create(
                "dashboard.read",
                "대시보드 조회",
                "대시보드 조회"
        ));
        PermissionGroup groupA = permissionGroupRepository.save(PermissionGroup.create(
                "대시보드 그룹 A",
                "대시보드 그룹 A",
                PermissionGroupType.CUSTOM
        ));
        PermissionGroup groupB = permissionGroupRepository.save(PermissionGroup.create(
                "대시보드 그룹 B",
                "대시보드 그룹 B",
                PermissionGroupType.CUSTOM
        ));
        accountGroupAssignmentRepository.saveAll(Set.of(
                AccountGroupAssignment.create(accountId, groupA.getIdOrThrow()),
                AccountGroupAssignment.create(accountId, groupB.getIdOrThrow())
        ));
        groupPermissionGrantRepository.saveAll(Set.of(
                GroupPermissionGrant.create(groupA.getIdOrThrow(), permission.getIdOrThrow(), PermissionScope.SELF),
                GroupPermissionGrant.create(groupB.getIdOrThrow(), permission.getIdOrThrow(), PermissionScope.SELF)
        ));
        flushAndClear();

        PermissionDetail result = permissionFinder.findPermissions(accountId);

        assertThat(result.permissions()).containsExactly(
                new GrantedPermissionDetail("dashboard.read", Set.of(PermissionScope.SELF))
        );
    }

    @Test
    @DisplayName("권한이 없는 계정은 빈 권한 목록을 반환한다")
    void returnEmptyPermissionsWhenNoAssignments() {
        Long accountId = createAccount("permission-user-4@abacus.co.kr");

        PermissionDetail result = permissionFinder.findPermissions(accountId);

        assertThat(result.accountId()).isEqualTo(accountId);
        assertThat(result.permissions()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 계정 ID도 빈 권한 목록을 반환한다")
    void returnEmptyPermissionsWhenAccountDoesNotExist() {
        PermissionDetail result = permissionFinder.findPermissions(9999L);

        assertThat(result.accountId()).isEqualTo(9999L);
        assertThat(result.permissions()).isEmpty();
    }

    private Long createAccount(String email) {
        Department department = departmentRepository.save(Department.create(
                "TEAM-" + Math.abs(email.hashCode()),
                "테스트 부서",
                DepartmentType.TEAM,
                null,
                null
        ));
        Employee employee = employeeRepository.save(Employee.create(
                department.getIdOrThrow(),
                "권한사용자",
                email,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(1990, 1, 1),
                EmployeePosition.ASSOCIATE,
                EmployeeType.FULL_TIME,
                EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                null
        ));
        Account account = accountRepository.save(Account.create(
                employee.getIdOrThrow(),
                email,
                passwordEncoder.encode("Password123!")
        ));
        return account.getIdOrThrow();
    }

}
