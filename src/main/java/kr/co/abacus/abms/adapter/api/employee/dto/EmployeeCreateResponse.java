package kr.co.abacus.abms.adapter.api.employee.dto;

public record EmployeeCreateResponse(Long employeeId) {

    public static EmployeeCreateResponse of(Long employeeId) {
        return new EmployeeCreateResponse(employeeId);
    }

}
