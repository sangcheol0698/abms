package kr.co.abacus.abms.account;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.access.AccountGroupAssignment;
import kr.co.abacus.abms.access.AccountGroupAssignmentRepository;
import kr.co.abacus.abms.access.PermissionGroup;
import kr.co.abacus.abms.department.Department;
import kr.co.abacus.abms.department.DepartmentRepository;
import kr.co.abacus.abms.department.DepartmentType;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.employee.EmployeeAvatar;
import kr.co.abacus.abms.employee.EmployeeGrade;
import kr.co.abacus.abms.employee.EmployeePosition;
import kr.co.abacus.abms.employee.EmployeeProfile;
import kr.co.abacus.abms.employee.EmployeeRepository;
import kr.co.abacus.abms.employee.EmployeeType;

/**
 * 계정이 하나도 없는 신규 환경에서 최고 관리자 계정을 만든다.
 * {@code ABMS_ADMIN_EMAIL}, {@code ABMS_ADMIN_PASSWORD} 환경 변수가 설정된 경우에만 동작한다.
 */
@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final AccountRepository accountRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final AccountGroupAssignmentRepository groupAssignmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminBootstrap(AccountRepository accountRepository, EmployeeRepository employeeRepository,
                          DepartmentRepository departmentRepository,
                          AccountGroupAssignmentRepository groupAssignmentRepository, PasswordEncoder passwordEncoder,
                          @Value("${abms.bootstrap.admin-email:}") String adminEmail,
                          @Value("${abms.bootstrap.admin-password:}") String adminPassword) {
        this.accountRepository = accountRepository;
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.groupAssignmentRepository = groupAssignmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminEmail.isBlank() || adminPassword.isBlank() || accountRepository.count() > 0) {
            return;
        }
        AccountService.validateStrength(adminPassword);
        Department company = departmentRepository.findAll().stream()
                .filter(d -> d.getParentId() == null)
                .findFirst()
                .orElseGet(() -> departmentRepository.save(Department.create("ROOT", "본사", DepartmentType.COMPANY, null)));
        Employee admin = employeeRepository.save(Employee.create(new EmployeeProfile(company.id(), "관리자", adminEmail,
                LocalDate.now(), LocalDate.of(1990, 1, 1), EmployeePosition.TEAM_LEADER, EmployeeType.FULL_TIME,
                EmployeeGrade.SENIOR, EmployeeAvatar.COBALT_WAVE, "초기 관리자 계정")));
        Account account = accountRepository.save(Account.create(admin.id(), adminEmail, passwordEncoder.encode(adminPassword)));
        groupAssignmentRepository.save(AccountGroupAssignment.of(account.id(), PermissionGroup.ADMIN_GROUP_ID));
        log.info("초기 관리자 계정을 생성했습니다: {}", adminEmail);
    }

}
