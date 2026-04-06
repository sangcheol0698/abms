package kr.co.abacus.abms.application.weeklyreport.dto.command;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

public record WeeklyReportGenerateCommand(
        @Nullable LocalDate weekStart,
        @Nullable LocalDate weekEnd) {

}
