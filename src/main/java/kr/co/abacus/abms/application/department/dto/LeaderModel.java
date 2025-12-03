package kr.co.abacus.abms.application.department.dto;

import java.util.UUID;

import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeePosition;

public record LeaderModel(
        UUID employeeId,
        String employeeName,
        EmployeePosition position,
        EmployeeAvatar avatar) {

}
