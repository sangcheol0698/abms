package kr.co.abacus.abms.adapter.api.summary;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.co.abacus.abms.adapter.api.summary.dto.CompanyMonthlyCostSummaryResponse;
import kr.co.abacus.abms.application.summary.inbound.CompanyMonthlyCostSummaryFinder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/companyMonthlyCostSummary")
public class CompanyMonthlyCostSummaryApi {

    private final CompanyMonthlyCostSummaryFinder summaryFinder;

    @GetMapping
    public CompanyMonthlyCostSummaryResponse getCompanyMonthlyCostSummary(
            @RequestParam("yearMonth") String yearMonth
    ) {
        return CompanyMonthlyCostSummaryResponse.of(summaryFinder.findByTargetMonth(yearMonth));
    }

    @GetMapping("/sixMonthTrend")
    public List<CompanyMonthlyCostSummaryResponse> getSixMonthsCompanyCostTrend(
            @RequestParam("yearMonth") String yearMonth
    ) {
        return summaryFinder.findRecentSixMonths(yearMonth).stream()
                .map(CompanyMonthlyCostSummaryResponse::of)
                .toList();
    }
}
