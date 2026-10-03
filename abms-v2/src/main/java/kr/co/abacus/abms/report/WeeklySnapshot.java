package kr.co.abacus.abms.report;

import java.time.LocalDate;
import java.util.List;

import kr.co.abacus.abms.common.domain.Money;

/**
 * 주간 보고서 생성에 쓰이는 데이터 스냅샷.
 */
public record WeeklySnapshot(
        LocalDate weekStart,
        LocalDate weekEnd,
        List<ProjectLine> activeProjects,
        List<RevenueLine> issuedThisWeek,
        List<RevenueLine> upcoming,
        List<RevenueLine> overdue,
        List<AssignmentLine> assignmentChanges,
        Money monthRevenue,
        Money monthCost,
        boolean monthCalculated
) {

    public Money issuedTotal() {
        return issuedThisWeek.stream().map(RevenueLine::amount).reduce(Money.ZERO, Money::plus);
    }

    public record ProjectLine(String code, String name, String status, String party, String department,
                              LocalDate startDate, LocalDate endDate, Money contractAmount, int memberCount) {
    }

    public record RevenueLine(String projectName, int sequence, String type, LocalDate date, Money amount) {
    }

    public record AssignmentLine(String projectName, String employeeName, String change, LocalDate date) {
    }

}
