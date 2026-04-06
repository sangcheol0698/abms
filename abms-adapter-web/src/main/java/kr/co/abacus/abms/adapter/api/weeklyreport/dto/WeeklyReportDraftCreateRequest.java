package kr.co.abacus.abms.adapter.api.weeklyreport.dto;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.application.weeklyreport.dto.command.WeeklyReportGenerateCommand;

public record WeeklyReportDraftCreateRequest(
        @Nullable LocalDate weekStart,
        @Nullable LocalDate weekEnd) {

    public WeeklyReportGenerateCommand toCommand() {
        return new WeeklyReportGenerateCommand(weekStart, weekEnd);
    }
}
