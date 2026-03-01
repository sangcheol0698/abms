package kr.co.abacus.abms.application.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.auth.dto.AuthenticatedUserInfo;
import kr.co.abacus.abms.application.auth.inbound.AuthFinder;
import kr.co.abacus.abms.application.auth.outbound.AccountRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.domain.account.Account;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.shared.Email;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AuthQueryService implements AuthFinder {

    private final AccountRepository accountRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public AuthenticatedUserInfo getCurrentUser(String username) {
        Email userEmail = new Email(username);

        return accountRepository.findByUsername(userEmail)
                .map(this::toUserInfo)
                .orElseGet(() -> fallbackUserInfo(userEmail.address()));
    }

    private AuthenticatedUserInfo toUserInfo(Account account) {
        return employeeRepository.findById(account.getEmployeeId())
                .map(employee -> new AuthenticatedUserInfo(
                        employee.getName(),
                        employee.getEmail().address()
                ))
                .orElseGet(() -> fallbackUserInfo(account.getUsername().address()));
    }

    private AuthenticatedUserInfo fallbackUserInfo(String email) {
        String localPart = email.split("@")[0];
        String normalizedName = localPart.isBlank() ? email : localPart;
        return new AuthenticatedUserInfo(normalizedName, email);
    }

}
