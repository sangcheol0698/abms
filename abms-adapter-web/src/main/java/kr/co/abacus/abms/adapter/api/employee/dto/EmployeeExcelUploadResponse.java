package kr.co.abacus.abms.adapter.api.employee.dto;

import java.util.List;

import kr.co.abacus.abms.application.employee.dto.EmployeeExcelUploadResult;

public record EmployeeExcelUploadResponse(
        int successCount,
        List<Failure> failures
) {

    public static EmployeeExcelUploadResponse of(EmployeeExcelUploadResult result) {
        List<Failure> failures = result.excelFailures().stream()
                .map(failure -> new Failure(failure.rowNumber(), failure.message()))
                .toList();
        return new EmployeeExcelUploadResponse(result.successCount(), failures);
    }

    public record Failure(int rowNumber, String message) {

    }

}
