package kr.co.abacus.abms.application.salaryhistory;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.employee.provided.EmployeeFinder;
import kr.co.abacus.abms.application.salaryhistory.provided.SalaryHistoryManager;
import kr.co.abacus.abms.application.salaryhistory.required.SalaryHistoryRepository;
import kr.co.abacus.abms.domain.salaryhistory.SalaryHistory;
import kr.co.abacus.abms.domain.shared.Money;

@Transactional
@RequiredArgsConstructor
@Service
public class SalaryHistoryModifyService implements SalaryHistoryManager {

    private final SalaryHistoryRepository salaryHistoryRepository;
    private final EmployeeFinder employeeFinder;

    @Override
    public void changeSalary(UUID employeeId, Money annualSalary, LocalDate startDate) {
        checkEmployeeExists(employeeId);

        salaryHistoryRepository.findCurrentSalaryByEmployeeId(employeeId).ifPresent(
            currentSalary -> closeCurrentSalaryBefore(startDate.minusDays(1), currentSalary)
        );

        SalaryHistory salaryHistory = SalaryHistory.startWith(employeeId, annualSalary, startDate);

        salaryHistoryRepository.save(salaryHistory);
    }

    private void checkEmployeeExists(UUID employeeId) {
        employeeFinder.find(employeeId);
    }

    private void closeCurrentSalaryBefore(LocalDate closedEndDate, SalaryHistory currentSalary) {
        currentSalary.close(closedEndDate);
        salaryHistoryRepository.save(currentSalary);
    }

}
