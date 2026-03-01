package kr.co.abacus.abms.application.employee.outbound;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import kr.co.abacus.abms.application.employee.dto.EmployeeCreateCommand;

public interface EmployeeExcelImporter {

    List<EmployeeCreateCommand> importEmployees(InputStream inputStream, Map<String, Long> departmentCodeMap);

}
