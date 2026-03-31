package kr.co.abacus.abms.adapter.api.employee.dto;

import jakarta.validation.constraints.NotNull;

public record EmployeeDepartmentTransferRequest(
        @NotNull Long departmentId) {

}
