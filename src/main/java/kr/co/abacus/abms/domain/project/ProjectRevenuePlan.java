package kr.co.abacus.abms.domain.project;

import java.time.LocalDate;

import javax.annotation.Nullable;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import kr.co.abacus.abms.domain.AbstractEntity;
import kr.co.abacus.abms.domain.shared.Money;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "project_revenue_plan")
public class ProjectRevenuePlan extends AbstractEntity {

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "revenue_month", nullable = false)
    private LocalDate revenueMonth;

    @Enumerated(EnumType.STRING)
    @Column(name = "revenue_type", nullable = false, length = 20)
    private RevenueType type;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "amount", nullable = false))
    private Money amount;

    @Column(name = "is_issued", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean isIssued;

    @Nullable
    @Column(name = "description")
    private LocalDate description;

}
