package kr.co.abacus.abms.application.weeklyreport.dto.query;

import java.util.List;

public record WeeklyReportInsightData(
        List<String> highlights,
        List<String> risks,
        List<String> nextActions) {

}
