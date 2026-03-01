package kr.co.abacus.abms.adapter.api.project.dto;

import java.util.List;

import kr.co.abacus.abms.application.project.dto.ProjectExcelUploadResult;

public record ProjectExcelUploadResponse(
        int successCount,
        List<Failure> failures
) {

    public static ProjectExcelUploadResponse of(ProjectExcelUploadResult result) {
        List<Failure> failures = result.excelFailures().stream()
                .map(failure -> new Failure(failure.rowNumber(), failure.message()))
                .toList();

        return new ProjectExcelUploadResponse(result.successCount(), failures);
    }

    public record Failure(int rowNumber, String message) {

    }

}
