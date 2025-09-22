package kr.co.abacus.abms.application.employee.provided;

import java.util.List;
import java.util.UUID;

import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeStatus;
import kr.co.abacus.abms.domain.employee.EmployeeType;

public record EmployeeSearchRequest(
    String name,
    List<EmployeePosition> positions,
    List<EmployeeType> types,
    List<EmployeeStatus> statuses,
    List<EmployeeGrade> grades,
    List<UUID> departmentIds
) {

}
