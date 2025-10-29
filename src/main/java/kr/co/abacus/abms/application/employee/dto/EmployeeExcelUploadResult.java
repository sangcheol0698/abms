package kr.co.abacus.abms.application.employee.dto;

import java.util.List;

public record EmployeeExcelUploadResult(int successCount, List<ExcelFailure> excelFailures) {

    public record ExcelFailure(int rowNumber, String message) {

    }
    
}
