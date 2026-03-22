package kr.co.abacus.abms.adapter.security.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.adapter.security.CustomUserDetails;
import kr.co.abacus.abms.application.auth.outbound.AccountRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.permission.inbound.PermissionFinder;
import kr.co.abacus.abms.domain.account.Account;
import kr.co.abacus.abms.domain.shared.Email;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final EmployeeRepository employeeRepository;
    private final PermissionFinder permissionFinder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsername(new Email(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        var employee = employeeRepository.findByIdAndDeletedFalse(account.getEmployeeId()).orElse(null);
        java.util.Map<String, java.util.Set<kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope>> permissionsByCode =
                new java.util.LinkedHashMap<>();
        permissionFinder.findPermissions(account.getIdOrThrow()).permissions()
                .forEach(permission -> permissionsByCode.put(permission.code(), permission.scopes()));

        return new CustomUserDetails(
                account,
                employee != null ? employee.getIdOrThrow() : null,
                employee != null ? employee.getDepartmentId() : null,
                permissionsByCode
        );
    }

}
