package kr.co.abacus.abms.application.payroll;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.employee.inbound.EmployeeFinder;
import kr.co.abacus.abms.application.payroll.inbound.PayrollManager;
import kr.co.abacus.abms.application.payroll.outbound.PayrollRepository;
import kr.co.abacus.abms.domain.payroll.Payroll;
import kr.co.abacus.abms.domain.shared.Money;

@Transactional
@RequiredArgsConstructor
@Service
public class PayrollModifyService implements PayrollManager {

    private final PayrollRepository payrollRepository;
    private final EmployeeFinder employeeFinder;

    @Override
    public void changeSalary(Long employeeId, Money annualSalary, LocalDate startDate) {
        checkEmployeeExists(employeeId);

        payrollRepository.lockByEmployeeIdAndTargetDate(employeeId, startDate).ifPresent(overlappingSalary -> {
            if (!startDate.isAfter(overlappingSalary.getPeriod().startDate())) {
                throw new IllegalArgumentException("새 연봉 적용 시작일은 기존 연봉 시작일 이후여야 합니다.");
            }
            closeCurrentSalaryBefore(startDate.minusDays(1), overlappingSalary);
        });

        if (payrollRepository.lockByEmployeeIdAndTargetDate(employeeId, startDate).isEmpty()
                && payrollRepository.lockNextSalaryByEmployeeId(employeeId, startDate).isPresent()) {
            throw new IllegalArgumentException("새 연봉 적용 시작일은 기존 연봉 시작일 이후여야 합니다.");
        }

        Payroll payroll = Payroll.create(employeeId, annualSalary, startDate);

        payrollRepository.save(payroll);
    }

    private void checkEmployeeExists(Long employeeId) {
        employeeFinder.find(employeeId);
    }

    private void closeCurrentSalaryBefore(LocalDate closedEndDate, Payroll currentSalary) {
        currentSalary.close(closedEndDate);
        payrollRepository.save(currentSalary);
    }

}
