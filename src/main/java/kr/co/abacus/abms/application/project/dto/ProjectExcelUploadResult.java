package kr.co.abacus.abms.application.project.dto;

import java.util.List;

public record ProjectExcelUploadResult(int successCount, List<ExcelFailure> excelFailures) {

    public record ExcelFailure(int rowNumber, String message) {

    }

}
