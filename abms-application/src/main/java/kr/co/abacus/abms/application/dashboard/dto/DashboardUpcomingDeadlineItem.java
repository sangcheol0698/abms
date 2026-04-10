package kr.co.abacus.abms.application.dashboard.dto;

import java.time.LocalDate;

public record DashboardUpcomingDeadlineItem(
        Long projectId,
        String name,
        String partyName,
        String status,
        String statusDescription,
        LocalDate endDate,
        long daysLeft
) {
}
