package kr.co.abacus.abms.adapter.api.summary;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.co.abacus.abms.adapter.api.summary.dto.MonthlyRevenueSummaryResponse;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.application.summary.inbound.MonthlyRevenueSummaryFinder;
import kr.co.abacus.abms.application.summary.inbound.MonthlyRevenueSummaryManager;
import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummary;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/monthlyRevenueSummary")
public class MonthlyRevenueSummaryApi {

    private final MonthlyRevenueSummaryFinder summaryFinder;
    private final ProjectRepository projectRepository;

    @GetMapping
    public MonthlyRevenueSummaryResponse getMonthlyRevenueSummary(
        @RequestParam("yearMonth") String yearMonth
    ) {
        MonthlyRevenueSummary summary = summaryFinder.findByTargetMonth(yearMonth);
        return MonthlyRevenueSummaryResponse.of(summary);
    }

}
