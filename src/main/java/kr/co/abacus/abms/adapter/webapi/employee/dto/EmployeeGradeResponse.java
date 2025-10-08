package kr.co.abacus.abms.adapter.webapi.employee.dto;

import kr.co.abacus.abms.domain.employee.EmployeeGrade;

public record EmployeeGradeResponse(
    String name,
    String description,
    int level
) {

    public static EmployeeGradeResponse of(EmployeeGrade employeeGrade) {
        return new EmployeeGradeResponse(employeeGrade.name(), employeeGrade.getDescription(), employeeGrade.getLevel());
    }

}
