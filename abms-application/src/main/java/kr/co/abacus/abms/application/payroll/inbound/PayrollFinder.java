package kr.co.abacus.abms.application.payroll.inbound;

import java.util.List;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.application.payroll.dto.EmployeePayrollCurrent;
import kr.co.abacus.abms.application.payroll.dto.EmployeePayrollHistoryItem;

public interface PayrollFinder {

    @Nullable EmployeePayrollCurrent findCurrentPayroll(Long employeeId);

    List<EmployeePayrollHistoryItem> findPayrollHistory(Long employeeId);

}
