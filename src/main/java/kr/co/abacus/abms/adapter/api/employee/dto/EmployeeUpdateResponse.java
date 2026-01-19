package kr.co.abacus.abms.adapter.api.employee.dto;

public record EmployeeUpdateResponse(
        Long employeeId) {

    public static EmployeeUpdateResponse of(Long employeeId) {
        return new EmployeeUpdateResponse(employeeId);
    }

}
