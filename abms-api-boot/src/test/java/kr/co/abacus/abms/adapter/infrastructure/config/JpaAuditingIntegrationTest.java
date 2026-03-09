package kr.co.abacus.abms.adapter.infrastructure.config;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import kr.co.abacus.abms.adapter.security.CustomUserDetails;
import kr.co.abacus.abms.application.auth.outbound.AccountRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.domain.account.Account;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.support.IntegrationTestBase;

@DisplayName("JPA 감사 설정")
class JpaAuditingIntegrationTest extends IntegrationTestBase {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AccountRepository accountRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("현재 로그인한 계정 ID를 생성자와 수정자에 저장한다")
    void should_storeCurrentAccountIdInAuditFields() {
        Employee actor = employeeRepository.save(createEmployee("auditor@abacus.co.kr", "감사 사용자"));
        Account account = accountRepository.save(Account.create(actor.getIdOrThrow(), "auditor@abacus.co.kr", "encoded-password"));
        flushAndClear();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new CustomUserDetails(account), null, List.of()));

        Employee target = employeeRepository.save(createEmployee("target@abacus.co.kr", "대상 사용자"));
        flushAndClear();

        Employee persisted = employeeRepository.findById(target.getIdOrThrow()).orElseThrow();
        assertThat(persisted.getCreatedBy()).isEqualTo(account.getIdOrThrow());
        assertThat(persisted.getUpdatedBy()).isEqualTo(account.getIdOrThrow());
    }

    private Employee createEmployee(String email, String name) {
        return Employee.create(
                1L,
                name,
                email,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(1990, 1, 1),
                EmployeePosition.ASSOCIATE,
                EmployeeType.FULL_TIME,
                EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                null);
    }

}
