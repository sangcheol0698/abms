package kr.co.abacus.abms.application.employee.outbound;

import java.util.List;
import java.util.Map;

import kr.co.abacus.abms.domain.employee.Employee;

public interface EmployeeExcelExporter {

    byte[] export(List<Employee> employees, Map<Long, String> departmentNames);

    byte[] exportSample();

}
