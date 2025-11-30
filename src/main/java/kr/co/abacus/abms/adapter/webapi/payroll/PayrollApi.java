package kr.co.abacus.abms.adapter.webapi.payroll;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.adapter.webapi.payroll.dto.PayrollCreateRequest;
import kr.co.abacus.abms.application.payroll.provided.PayrollManager;
import kr.co.abacus.abms.domain.shared.Money;

@RequiredArgsConstructor
@RestController
public class PayrollApi {

    private final PayrollManager payrollManager;

    @PostMapping("/api/salary-history")
    public ResponseEntity<Void> changeSalary(@RequestBody @Valid PayrollCreateRequest request) {
        payrollManager.changeSalary(request.employeeId(), Money.wons(request.annualSalary()), request.startDate());

        return ResponseEntity.noContent().build();
    }
}
