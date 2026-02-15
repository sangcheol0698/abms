package kr.co.abacus.abms.domain.summary;

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

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "summary_date", nullable = false)
    private String summaryDate;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "revenue_amount", nullable = false))
    private Money revenueAmount;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "cost_amount", nullable = false))
    private Money costAmount;

    // todo: 적자 처리를 위해 마이너스 가능하게 해야함
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "profit_amount", nullable = false))
    private Money profitAmount;

}
