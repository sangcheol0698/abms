package kr.co.abacus.abms.application.permission.outbound;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import kr.co.abacus.abms.application.auth.outbound.AccountRepository;
import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
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

@DisplayName("권한 저장소 (PermissionRepository)")
class PermissionRepositoryTest extends IntegrationTestBase {

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
    @DisplayName("활성 권한과 그룹만 ID 목록으로 조회한다")
    void findActivePermissionsAndGroupsByIds() {
        Permission activePermission = permissionRepository.save(Permission.create("employee.read", "직원 조회", "직원 조회"));
        Permission deletedPermission = permissionRepository.save(Permission.create("employee.update", "직원 수정", "직원 수정"));
        deletedPermission.softDelete(1L);

        PermissionGroup activeGroup = permissionGroupRepository.save(PermissionGroup.create(
                "활성 그룹",
                "활성 그룹",
                PermissionGroupType.CUSTOM
        ));
        PermissionGroup deletedGroup = permissionGroupRepository.save(PermissionGroup.create(
                "삭제 그룹",
                "삭제 그룹",
                PermissionGroupType.CUSTOM
        ));
        deletedGroup.softDelete(1L);
        flushAndClear();

        List<Permission> permissions = permissionRepository.findAllByIdInAndDeletedFalse(List.of(
                activePermission.getIdOrThrow(),
                deletedPermission.getIdOrThrow()
        ));
        List<PermissionGroup> permissionGroups = permissionGroupRepository.findAllByIdInAndDeletedFalse(List.of(
                activeGroup.getIdOrThrow(),
                deletedGroup.getIdOrThrow()
        ));

        assertThat(permissions).extracting(Permission::getCode).containsExactly("employee.read");
        assertThat(permissionGroups).extracting(PermissionGroup::getName).containsExactly("활성 그룹");
    }

    @Test
    @DisplayName("활성 계정-그룹 연결과 grant만 조회한다")
    void findActiveAssignmentsAndGrants() {
        Long accountId = createAccount("permission-repo-user@abacus.co.kr");
        Permission permission = permissionRepository.save(Permission.create("project.read", "프로젝트 조회", "프로젝트 조회"));
        PermissionGroup activeGroup = permissionGroupRepository.save(PermissionGroup.create(
                "활성 그룹",
                "활성 그룹",
                PermissionGroupType.CUSTOM
        ));
        PermissionGroup deletedGroup = permissionGroupRepository.save(PermissionGroup.create(
                "삭제 그룹",
                "삭제 그룹",
                PermissionGroupType.CUSTOM
        ));

        AccountGroupAssignment activeAssignment =
                accountGroupAssignmentRepository.save(AccountGroupAssignment.create(accountId, activeGroup.getIdOrThrow()));
        AccountGroupAssignment deletedAssignment =
                accountGroupAssignmentRepository.save(AccountGroupAssignment.create(accountId, deletedGroup.getIdOrThrow()));

        GroupPermissionGrant activeGrant = groupPermissionGrantRepository.save(GroupPermissionGrant.create(
                activeGroup.getIdOrThrow(),
                permission.getIdOrThrow(),
                PermissionScope.OWN_DEPARTMENT
        ));
        GroupPermissionGrant deletedGrant = groupPermissionGrantRepository.save(GroupPermissionGrant.create(
                deletedGroup.getIdOrThrow(),
                permission.getIdOrThrow(),
                PermissionScope.SELF
        ));

        deletedAssignment.softDelete(1L);
        deletedGrant.softDelete(1L);
        flushAndClear();

        List<AccountGroupAssignment> assignments = accountGroupAssignmentRepository.findAllByAccountIdAndDeletedFalse(accountId);
        List<GroupPermissionGrant> grants = groupPermissionGrantRepository.findAllByPermissionGroupIdInAndDeletedFalse(
                List.of(activeGroup.getIdOrThrow(), deletedGroup.getIdOrThrow())
        );

        assertThat(assignments)
                .extracting(AccountGroupAssignment::getPermissionGroupId)
                .containsExactly(activeGroup.getIdOrThrow());
        assertThat(grants)
                .extracting(GroupPermissionGrant::getScope)
                .containsExactly(PermissionScope.OWN_DEPARTMENT);
        assertThat(Set.of(activeAssignment.getPermissionGroupId())).contains(activeGroup.getIdOrThrow());
        assertThat(activeGrant.getScope()).isEqualTo(PermissionScope.OWN_DEPARTMENT);
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
                "권한저장소사용자",
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
