package kr.co.abacus.abms.adapter.webapi.salaryhistory;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.adapter.webapi.salaryhistory.dto.SalaryHistoryCreateRequest;
import kr.co.abacus.abms.application.salaryhistory.provided.SalaryHistoryManager;
import kr.co.abacus.abms.domain.shared.Money;

@RequiredArgsConstructor
@RestController
public class SalaryHistoryApi {

    private final SalaryHistoryManager salaryHistoryManager;

    @PostMapping("/api/salary-history")
    public ResponseEntity<Void> changeSalary(@RequestBody @Valid SalaryHistoryCreateRequest request) {
        salaryHistoryManager.changeSalary(request.employeeId(), Money.wons(request.annualSalary()), request.startDate());

        return ResponseEntity.noContent().build();
    }
}
