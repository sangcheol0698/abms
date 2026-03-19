package kr.co.abacus.abms.application.payroll;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.payroll.dto.EmployeePayrollCurrent;
import kr.co.abacus.abms.application.payroll.dto.EmployeePayrollHistoryItem;
import kr.co.abacus.abms.application.payroll.inbound.PayrollFinder;
import kr.co.abacus.abms.application.payroll.outbound.PayrollRepository;
import kr.co.abacus.abms.domain.payroll.Payroll;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PayrollQueryService implements PayrollFinder {

    private static final BigDecimal MONTHS_IN_YEAR = BigDecimal.valueOf(12L);

    private final PayrollRepository payrollRepository;

    @Override
    public @Nullable EmployeePayrollCurrent findCurrentPayroll(Long employeeId) {
        LocalDate today = LocalDate.now();
        return payrollRepository.findByEmployeeIdAndTargetDate(employeeId, today)
                .map(payroll -> new EmployeePayrollCurrent(
                        payroll.getEmployeeId(),
                        payroll.getAnnualSalary().amount().longValue(),
                        payroll.getAnnualSalary().divide(MONTHS_IN_YEAR).amount().longValue(),
                        payroll.getPeriod().startDate(),
                        "CURRENT"
                ))
                .orElse(null);
    }

    @Override
    public List<EmployeePayrollHistoryItem> findPayrollHistory(Long employeeId) {
        LocalDate today = LocalDate.now();
        return payrollRepository.findAllByEmployeeId(employeeId).stream()
                .map(payroll -> toHistoryItem(payroll, today))
                .toList();
    }

    private EmployeePayrollHistoryItem toHistoryItem(Payroll payroll, LocalDate today) {
        return new EmployeePayrollHistoryItem(
                payroll.getEmployeeId(),
                payroll.getAnnualSalary().amount().longValue(),
                payroll.getAnnualSalary().divide(MONTHS_IN_YEAR).amount().longValue(),
                payroll.getPeriod().startDate(),
                payroll.getPeriod().endDate(),
                resolveStatus(payroll, today)
        );
    }

    private String resolveStatus(Payroll payroll, LocalDate today) {
        if (payroll.getPeriod().startDate().isAfter(today)) {
            return "SCHEDULED";
        }
        if (payroll.getPeriod().endDate() == null || !payroll.getPeriod().endDate().isBefore(today)) {
            return "CURRENT";
        }
        return "ENDED";
    }

}
