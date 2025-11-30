package kr.co.abacus.abms.adapter.webapi.department.dto;

import java.time.LocalDate;
import java.util.UUID;

import kr.co.abacus.abms.domain.employee.Employee;

public record EmployeeResponse(
        UUID employeeId,
        UUID departmentId,
        String employeeName,
        String email,
        String position,
        String type,
        String status,
        String grade,
        LocalDate joinDate,
        LocalDate birthDate,
        String avatarCode,
        String avatarLabel) {

    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getDepartmentId(),
                employee.getName(),
                employee.getEmail().address(),
                employee.getPosition().getDescription(),
                employee.getType().getDescription(),
                employee.getStatus().getDescription(),
                employee.getGrade().getDescription(),
                employee.getJoinDate(),
                employee.getBirthDate(),
                employee.getAvatar().name(),
                employee.getAvatar().getDisplayName());
    }
}
