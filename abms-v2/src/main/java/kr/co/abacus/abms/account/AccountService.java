package kr.co.abacus.abms.account;

import java.security.SecureRandom;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.access.AccountGroupAssignment;
import kr.co.abacus.abms.access.AccountGroupAssignmentRepository;
import kr.co.abacus.abms.access.PermissionGroup;
import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.NotFoundException;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.employee.EmployeeRepository;
import kr.co.abacus.abms.notification.NotificationService;
import kr.co.abacus.abms.notification.NotificationType;

/**
 * 계정 발급/비밀번호 관리.
 * 계정은 관리자가 직원에게 발급하며, 발급 시 임시 비밀번호가 한 번만 표시된다.
 */
@Service
@Transactional
public class AccountService {

    private static final String TEMP_PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AccountRepository accountRepository;
    private final EmployeeRepository employeeRepository;
    private final AccountGroupAssignmentRepository groupAssignmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    public AccountService(AccountRepository accountRepository, EmployeeRepository employeeRepository,
                          AccountGroupAssignmentRepository groupAssignmentRepository, PasswordEncoder passwordEncoder,
                          NotificationService notificationService) {
        this.accountRepository = accountRepository;
        this.employeeRepository = employeeRepository;
        this.groupAssignmentRepository = groupAssignmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<Account> all() {
        return accountRepository.findAllByOrderByUsernameAsc();
    }

    @Transactional(readOnly = true)
    public Account get(Long id) {
        return accountRepository.findById(id).orElseThrow(() -> NotFoundException.of("계정", id));
    }

    /** 계정이 없는 재직 직원 목록 */
    @Transactional(readOnly = true)
    public List<Employee> employeesWithoutAccount() {
        return employeeRepository.findAllByDeletedFalseOrderByNameAsc().stream()
                .filter(e -> !e.isResigned())
                .filter(e -> !accountRepository.existsByEmployeeId(e.id()))
                .toList();
    }

    /** 직원 계정을 발급하고 임시 비밀번호를 반환한다. 기본 권한 그룹이 할당된다. */
    public IssuedAccount issue(Long employeeId) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(employeeId)
                .orElseThrow(() -> NotFoundException.of("직원", employeeId));
        if (employee.isResigned()) {
            throw new BusinessException("퇴사한 직원에게는 계정을 발급할 수 없습니다.");
        }
        if (accountRepository.existsByEmployeeId(employeeId)) {
            throw new BusinessException("이미 계정이 있는 직원입니다.");
        }
        if (accountRepository.existsByUsername(employee.getEmail())) {
            throw new BusinessException("같은 아이디의 계정이 이미 있습니다: " + employee.getEmail());
        }
        String tempPassword = temporaryPassword();
        Account account = accountRepository.save(Account.create(employeeId, employee.getEmail(), passwordEncoder.encode(tempPassword)));
        groupAssignmentRepository.save(AccountGroupAssignment.of(account.id(), PermissionGroup.DEFAULT_GROUP_ID));
        notificationService.notifyAccount(account.id(), NotificationType.INFO, "ABMS 계정이 발급되었습니다.",
                "보안을 위해 임시 비밀번호를 변경해 주세요.", "/me");
        return new IssuedAccount(account, tempPassword);
    }

    public String resetPassword(Long accountId) {
        Account account = get(accountId);
        String tempPassword = temporaryPassword();
        account.changePassword(passwordEncoder.encode(tempPassword));
        account.unlock();
        return tempPassword;
    }

    public void changePassword(Long accountId, String currentPassword, String newPassword) {
        Account account = get(accountId);
        if (!passwordEncoder.matches(currentPassword, account.getPassword())) {
            throw new BusinessException("현재 비밀번호가 일치하지 않습니다.");
        }
        if (passwordEncoder.matches(newPassword, account.getPassword())) {
            throw new BusinessException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }
        validateStrength(newPassword);
        account.changePassword(passwordEncoder.encode(newPassword));
    }

    public void enable(Long accountId) {
        get(accountId).enable();
    }

    public void disable(Long accountId, Long actorAccountId) {
        if (accountId.equals(actorAccountId)) {
            throw new BusinessException("본인 계정은 비활성화할 수 없습니다.");
        }
        get(accountId).disable();
    }

    public void unlock(Long accountId) {
        get(accountId).unlock();
    }

    static void validateStrength(String password) {
        if (password.length() < 8) {
            throw new BusinessException("비밀번호는 8자 이상이어야 합니다.");
        }
        boolean letter = password.chars().anyMatch(Character::isLetter);
        boolean digit = password.chars().anyMatch(Character::isDigit);
        if (!letter || !digit) {
            throw new BusinessException("비밀번호는 영문과 숫자를 모두 포함해야 합니다.");
        }
    }

    private static String temporaryPassword() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(TEMP_PASSWORD_CHARS.charAt(RANDOM.nextInt(TEMP_PASSWORD_CHARS.length())));
        }
        // 영문+숫자 포함 보장
        sb.setCharAt(RANDOM.nextInt(6), (char) ('2' + RANDOM.nextInt(8)));
        sb.setCharAt(6 + RANDOM.nextInt(6), (char) ('a' + RANDOM.nextInt(26)));
        return sb.toString();
    }

    public record IssuedAccount(Account account, String temporaryPassword) {
    }

}
