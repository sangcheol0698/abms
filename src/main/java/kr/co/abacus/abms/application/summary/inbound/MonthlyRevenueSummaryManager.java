package kr.co.abacus.abms.application.summary.inbound;

import java.time.LocalDate;
import java.util.List;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.shared.Money;
import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummary;

public interface MonthlyRevenueSummaryManager {

    MonthlyRevenueSummary calculateMonthlySummary(LocalDate targetMonth);

    Money calculateRevenue(LocalDate targetMonth);

    Money calculateTotalCost(LocalDate targetMonth);

    Money calculateProfit(@Nullable Money revenue, @Nullable Money cost);

    List<MonthlyRevenueSummary> calculateMonthlySummaryByProject(LocalDate targetMonth);

    MonthlyRevenueSummary calculateSummaryForProject(Project project, LocalDate targetMonth);

}
