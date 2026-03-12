package kr.co.abacus.abms.application.auth;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import kr.co.abacus.abms.application.auth.dto.AuthenticatedUserInfo;
import kr.co.abacus.abms.application.auth.inbound.AuthFinder;
import kr.co.abacus.abms.application.auth.outbound.AccountRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.permission.dto.GrantedPermissionDetail;
import kr.co.abacus.abms.application.permission.outbound.AccountGroupAssignmentRepository;
import kr.co.abacus.abms.application.permission.outbound.GroupPermissionGrantRepository;
import kr.co.abacus.abms.application.permission.outbound.PermissionGroupRepository;
import kr.co.abacus.abms.application.permission.outbound.PermissionRepository;
import kr.co.abacus.abms.domain.account.Account;
import kr.co.abacus.abms.domain.account.AccountNotFoundException;
import kr.co.abacus.abms.domain.accountgroupassignment.AccountGroupAssignment;
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
import kr.co.abacus.abms.support.IntegrationTestBase;

@DisplayName("인증 조회 (AuthFinder)")
class AuthFinderTest extends IntegrationTestBase {

    @Autowired
    private AuthFinder authFinder;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PermissionGroupRepository permissionGroupRepository;

    @Autowired
    private AccountGroupAssignmentRepository accountGroupAssignmentRepository;

    @Autowired
    private GroupPermissionGrantRepository groupPermissionGrantRepository;

    @Test
    @DisplayName("현재 사용자 정보를 직원 정보와 권한 목록으로 조회한다")
    void getCurrentUser() {
        String email = "auth-finder@abms.co";
        Employee employee = employeeRepository.save(createEmployee(email, "조회사용자"));
        Account account = accountRepository.save(Account.create(
                employee.getIdOrThrow(),
                email,
                passwordEncoder.encode("Password123!")
        ));

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
        groupPermissionGrantRepository.save(GroupPermissionGrant.create(
                permissionGroup.getIdOrThrow(),
                permission.getIdOrThrow(),
                PermissionScope.ALL
        ));
        flushAndClear();

        AuthenticatedUserInfo currentUser = authFinder.getCurrentUser(email);

        assertThat(currentUser.name()).isEqualTo("조회사용자");
        assertThat(currentUser.email()).isEqualTo(email);
        assertThat(currentUser.employeeId()).isEqualTo(employee.getIdOrThrow());
        assertThat(currentUser.departmentId()).isEqualTo(employee.getDepartmentId());
        assertThat(currentUser.permissions()).containsExactly(
                new GrantedPermissionDetail("employee.read", java.util.Set.of(PermissionScope.ALL))
        );
    }

    @Test
    @DisplayName("계정이 없으면 이메일 local-part를 이름으로 하는 fallback 정보를 반환한다")
    void getCurrentUser_fallbackForUnknownAccount() {
        AuthenticatedUserInfo currentUser = authFinder.getCurrentUser("fallback-user@abms.co");

        assertThat(currentUser.name()).isEqualTo("fallback-user");
        assertThat(currentUser.email()).isEqualTo("fallback-user@abms.co");
        assertThat(currentUser.employeeId()).isNull();
        assertThat(currentUser.departmentId()).isNull();
        assertThat(currentUser.permissions()).isEmpty();
    }

    @Test
    @DisplayName("삭제된 계정은 현재 사용자 조회에서 fallback 정보로 처리한다")
    void getCurrentUser_fallbackForDeletedAccount() {
        String email = "deleted-account@abms.co";
        Employee employee = employeeRepository.save(createEmployee(email, "삭제계정사용자"));
        Account account = accountRepository.save(Account.create(
                employee.getIdOrThrow(),
                email,
                passwordEncoder.encode("Password123!")
        ));
        account.softDelete(null);
        flushAndClear();

        AuthenticatedUserInfo currentUser = authFinder.getCurrentUser(email);

        assertThat(currentUser.name()).isEqualTo("deleted-account");
        assertThat(currentUser.email()).isEqualTo(email);
        assertThat(currentUser.employeeId()).isNull();
        assertThat(currentUser.departmentId()).isNull();
        assertThat(currentUser.permissions()).isEmpty();
    }

    @Test
    @DisplayName("삭제된 직원에 연결된 계정은 fallback 사용자 정보로 조회한다")
    void getCurrentUser_fallbackForDeletedEmployee() {
        String email = "deleted-employee@abms.co";
        Employee employee = employeeRepository.save(createEmployee(email, "삭제직원사용자"));
        Account account = accountRepository.save(Account.create(
                employee.getIdOrThrow(),
                email,
                passwordEncoder.encode("Password123!")
        ));
        employee.softDelete(null);
        flushAndClear();

        AuthenticatedUserInfo currentUser = authFinder.getCurrentUser(email);

        assertThat(currentUser.name()).isEqualTo("deleted-employee");
        assertThat(currentUser.email()).isEqualTo(email);
        assertThat(currentUser.employeeId()).isNull();
        assertThat(currentUser.departmentId()).isNull();
        assertThat(currentUser.permissions()).isEmpty();
    }

    @Test
    @DisplayName("현재 계정 ID를 조회한다")
    void getCurrentAccountId() {
        String email = "account-id-user@abms.co";
        Employee employee = employeeRepository.save(createEmployee(email, "계정ID사용자"));
        Account account = accountRepository.save(Account.create(
                employee.getIdOrThrow(),
                email,
                passwordEncoder.encode("Password123!")
        ));
        flushAndClear();

        Long currentAccountId = authFinder.getCurrentAccountId(email);

        assertThat(currentAccountId).isEqualTo(account.getId());
    }

    @Test
    @DisplayName("현재 계정 ID 조회 시 계정이 없으면 예외가 발생한다")
    void getCurrentAccountId_notFound() {
        assertThatThrownBy(() -> authFinder.getCurrentAccountId("missing-account@abms.co"))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("계정을 찾을 수 없습니다: missing-account@abms.co");
    }

    @Test
    @DisplayName("삭제된 계정의 현재 계정 ID 조회는 실패한다")
    void getCurrentAccountId_deletedAccount() {
        String email = "deleted-account-id@abms.co";
        Employee employee = employeeRepository.save(createEmployee(email, "삭제계정ID사용자"));
        Account account = accountRepository.save(Account.create(
                employee.getIdOrThrow(),
                email,
                passwordEncoder.encode("Password123!")
        ));
        account.softDelete(null);
        flushAndClear();

        assertThatThrownBy(() -> authFinder.getCurrentAccountId(email))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("계정을 찾을 수 없습니다: " + email);
    }

    private Employee createEmployee(String email, String name) {
        return Employee.create(
                1L,
                name,
                email,
                LocalDate.of(2025, 1, 2),
                LocalDate.of(1995, 6, 10),
                EmployeePosition.ASSOCIATE,
                EmployeeType.FULL_TIME,
                EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                null
        );
    }

}
