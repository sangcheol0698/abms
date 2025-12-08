package kr.co.abacus.abms.application.employee.dto;

import java.time.LocalDate;
import java.util.UUID;

import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeStatus;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.domain.shared.Email;

public record EmployeeResponse(
    UUID departmentId,
    String departmentName,
    UUID employeeId,
    String name,
    Email email,
    LocalDate joinDate,
    LocalDate birthDate,
    EmployeePosition position,
    EmployeeStatus status,
    EmployeeGrade grade,
    EmployeeType type,
    EmployeeAvatar avatar,
    String memo
) {

    public static EmployeeResponse of(Employee employee, Department department) {
        return new EmployeeResponse(
            employee.getDepartmentId(),
            department.getName(),
            employee.getId(),
            employee.getName(),
            employee.getEmail(),
            employee.getJoinDate(),
            employee.getBirthDate(),
            employee.getPosition(),
            employee.getStatus(),
            employee.getGrade(),
            employee.getType(),
            employee.getAvatar(),
            employee.getMemo()
        );
    }

}
