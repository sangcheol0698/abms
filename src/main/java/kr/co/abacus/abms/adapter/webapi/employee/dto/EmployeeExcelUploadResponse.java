package kr.co.abacus.abms.adapter.webapi.employee.dto;

import java.util.List;

public record EmployeeExcelUploadResponse(
    int successCount,
    List<Failure> failures
) {

    public record Failure(int rowNumber, String message) {
    }
}
