package kr.co.abacus.abms.adapter.api.weeklyreport.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import kr.co.abacus.abms.application.weeklyreport.dto.command.WeeklyReportUpdateCommand;

public record WeeklyReportDraftUpdateRequest(
        @NotBlank(message = "보고서 제목은 필수입니다.")
        @Size(max = 200, message = "보고서 제목은 200자를 초과할 수 없습니다.")
        String title,
        String reportMarkdown) {

    public WeeklyReportUpdateCommand toCommand() {
        return new WeeklyReportUpdateCommand(title, reportMarkdown);
    }
}
