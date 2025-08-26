package kr.co.abacus.abms.adapter.webapi.employee.dto;

import java.util.UUID;

import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.employee.Employee;

public record EmployeeResponse(
    UUID departmentId,
    String departmentName,
    String employeeId,
    String name,
    String email,
    String position,
    String status,
    String grade,
    String type,
    String memo
) {

    public static EmployeeResponse of(Employee employee, Department department) {
        return new EmployeeResponse(
            employee.getDepartmentId(),
            department.getName(),
            employee.getId().toString(),
            employee.getName(),
            employee.getEmail().address(),
            employee.getPosition().getDescription(),
            employee.getStatus().getDescription(),
            employee.getGrade().getDescription(),
            employee.getType().getDescription(),
            employee.getMemo()
        );
    }

}
