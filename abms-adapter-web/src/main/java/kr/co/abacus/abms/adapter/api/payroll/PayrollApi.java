package kr.co.abacus.abms.adapter.api.payroll;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.adapter.api.payroll.dto.EmployeePayrollChangeRequest;
import kr.co.abacus.abms.adapter.security.CurrentActorResolver;
import kr.co.abacus.abms.adapter.api.payroll.dto.EmployeePayrollCurrentResponse;
import kr.co.abacus.abms.adapter.api.payroll.dto.EmployeePayrollHistoryResponse;
import kr.co.abacus.abms.adapter.api.payroll.dto.PayrollCreateRequest;
import kr.co.abacus.abms.application.auth.CurrentActor;
import kr.co.abacus.abms.application.employee.inbound.EmployeeFinder;
import kr.co.abacus.abms.application.payroll.inbound.PayrollFinder;
import kr.co.abacus.abms.application.payroll.inbound.PayrollManager;
import kr.co.abacus.abms.domain.payroll.PayrollNotFoundException;
import kr.co.abacus.abms.domain.shared.Money;

@RequiredArgsConstructor
@RestController
public class PayrollApi {

    private final PayrollFinder payrollFinder;
    private final PayrollManager payrollManager;
    private final EmployeeFinder employeeFinder;
    private final CurrentActorResolver currentActorResolver;

    @PreAuthorize("@permissionAuthorizationChecker.hasPermission(authentication, 'employee.read')")
    @GetMapping("/api/employees/{employeeId}/payroll")
    public EmployeePayrollCurrentResponse getCurrentPayroll(@PathVariable Long employeeId, Authentication authentication) {
        authorizeRead(employeeId, currentActorResolver.resolve(authentication));
        var currentPayroll = payrollFinder.findCurrentPayroll(employeeId);
        if (currentPayroll == null) {
            throw new PayrollNotFoundException("현재 연봉 정보가 없습니다: " + employeeId);
        }
        return EmployeePayrollCurrentResponse.from(currentPayroll);
    }

    @PreAuthorize("@permissionAuthorizationChecker.hasPermission(authentication, 'employee.read')")
    @GetMapping("/api/employees/{employeeId}/payroll-history")
    public List<EmployeePayrollHistoryResponse> getPayrollHistory(@PathVariable Long employeeId, Authentication authentication) {
        authorizeRead(employeeId, currentActorResolver.resolve(authentication));
        return payrollFinder.findPayrollHistory(employeeId).stream()
                .map(EmployeePayrollHistoryResponse::from)
                .toList();
    }

    @PreAuthorize("@permissionAuthorizationChecker.hasPermission(authentication, 'employee.write')")
    @PostMapping("/api/employees/{employeeId}/payroll-history")
    public ResponseEntity<Void> changeSalary(
            @PathVariable Long employeeId,
            @RequestBody @Valid EmployeePayrollChangeRequest request,
            Authentication authentication
    ) {
        payrollManager.changeSalary(
                currentActorResolver.resolve(authentication),
                employeeId,
                Money.wons(request.annualSalary()),
                request.startDate()
        );
        return ResponseEntity.noContent().build();
    }

    @Deprecated
    @PreAuthorize("@permissionAuthorizationChecker.hasPermission(authentication, 'employee.write')")
    @PostMapping("/api/salary-history")
    public ResponseEntity<Void> changeSalary(@RequestBody @Valid PayrollCreateRequest request, Authentication authentication) {
        payrollManager.changeSalary(
                currentActorResolver.resolve(authentication),
                request.employeeId(),
                Money.wons(request.annualSalary()),
                request.startDate()
        );
        return ResponseEntity.noContent().build();
    }

    private void authorizeRead(Long employeeId, CurrentActor actor) {
        if (employeeFinder.find(employeeId) == null) {
            throw new PayrollNotFoundException("현재 연봉 정보가 없습니다: " + employeeId);
        }
        if (employeeFinder.findEmployeeDetail(employeeId, actor) == null) {
            throw new org.springframework.security.access.AccessDeniedException("직원 조회 권한 범위를 벗어났습니다.");
        }
    }
}
