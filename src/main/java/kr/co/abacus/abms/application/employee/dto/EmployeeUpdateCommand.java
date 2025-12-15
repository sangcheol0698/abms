package kr.co.abacus.abms.application.employee.dto;

import java.time.LocalDate;
import java.util.UUID;

import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeType;

public record EmployeeUpdateCommand(
     UUID departmentId,
     String email,
     String name,
     LocalDate joinDate,
     LocalDate birthDate,
     EmployeePosition position,
     EmployeeType type,
     EmployeeGrade grade,
     EmployeeAvatar avatar,
     String memo
) {

}
