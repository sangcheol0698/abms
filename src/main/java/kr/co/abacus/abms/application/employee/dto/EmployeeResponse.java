package kr.co.abacus.abms.application.employee.dto;

import java.time.LocalDate;
import java.util.UUID;

import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;

public record EmployeeResponse(
    UUID departmentId,
    String departmentName,
    UUID employeeId,
    String name,
    String email,
    LocalDate joinDate,
    LocalDate birthDate,
    String position,
    String status,
    String grade,
    String type,
    EmployeeAvatar avatar,
    String memo
) {

    public static EmployeeResponse of(Employee employee, Department department) {
        return new EmployeeResponse(
            employee.getDepartmentId(),
            department.getName(),
            employee.getId(),
            employee.getName(),
            employee.getEmail().address(),
            employee.getJoinDate(),
            employee.getBirthDate(),
            employee.getPosition().getDescription(),
            employee.getStatus().getDescription(),
            employee.getGrade().getDescription(),
            employee.getType().getDescription(),
            employee.getAvatar(),
            employee.getMemo()
        );
    }

}
