package kr.co.abacus.abms.domain.summary;

import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import kr.co.abacus.abms.domain.AbstractEntity;
import kr.co.abacus.abms.domain.shared.Money;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tb_monthly_revenue_summary")
public class MonthlyRevenueSummary extends AbstractEntity {

    @Column(name = "project_id", nullable = false, comment = "프로젝트ID")
    private Long projectId;

    @Column(name = "summary_date", nullable = false, comment = "집계대상일자")
    private LocalDate summaryDate;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "revenue_amount", nullable = false, comment = "매출금액"))
    private Money revenueAmount;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "cost_amount", nullable = false, comment = "비용금액"))
    private Money costAmount;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "profit_amount", nullable = false, comment = "이익금액"))
    private Money profitAmount;

    public static MonthlyRevenueSummary create(MonthlyRevenueSummaryCreateRequest createRequest) {
        MonthlyRevenueSummary summary = new MonthlyRevenueSummary();

        summary.projectId = Objects.requireNonNull(createRequest.projectId());
        summary.summaryDate = Objects.requireNonNull(createRequest.summaryDate());
        summary.revenueAmount = Objects.requireNonNull(createRequest.revenueAmount());
        summary.costAmount = Objects.requireNonNull(createRequest.costAmount());
        summary.profitAmount = Objects.requireNonNull(createRequest.profitAmount());

        return summary;
    }
}
