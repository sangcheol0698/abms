package kr.co.abacus.abms.project;

import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLRestriction;
import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.common.audit.Auditable;
import kr.co.abacus.abms.common.audit.Auditable.AuditRef;
import kr.co.abacus.abms.common.domain.BaseEntity;
import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.Money;

/**
 * 프로젝트 매출(청구) 계획.
 * <p>
 * 매출은 프로젝트 기간을 월할하지 않고, <b>세금계산서가 발행된 청구 계획</b>의 청구일이 속한 월에 인식한다.
 */
@Entity
@Table(name = "tb_project_revenue_plan")
@SQLRestriction("deleted = false")
public class ProjectRevenuePlan extends BaseEntity implements Auditable {

    @Column(nullable = false)
    private Long projectId;

    @Column(name = "plan_sequence", nullable = false)
    private int sequence;

    @Column(nullable = false)
    private LocalDate revenueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "revenue_type", nullable = false, length = 30)
    private RevenueType type;

    @Column(nullable = false)
    private Money amount;

    @Column(nullable = false)
    private boolean issued;

    private @Nullable String memo;

    protected ProjectRevenuePlan() {
    }

    public static ProjectRevenuePlan create(Long projectId, RevenuePlanInfo info) {
        ProjectRevenuePlan plan = new ProjectRevenuePlan();
        plan.projectId = Objects.requireNonNull(projectId);
        plan.apply(info);
        plan.issued = false;
        return plan;
    }

    public void update(RevenuePlanInfo info) {
        apply(info);
    }

    private void apply(RevenuePlanInfo info) {
        if (info.sequence() < 1) {
            throw new BusinessException("차수는 1 이상이어야 합니다.");
        }
        if (info.amount().isNegative()) {
            throw new BusinessException("매출 금액은 음수일 수 없습니다.");
        }
        this.sequence = info.sequence();
        this.revenueDate = Objects.requireNonNull(info.revenueDate(), "청구일은 필수입니다.");
        this.type = Objects.requireNonNull(info.type());
        this.amount = info.amount();
        this.memo = info.memo() == null || info.memo().isBlank() ? null : info.memo().trim();
    }

    public void issue() {
        if (issued) {
            throw new BusinessException("이미 발행된 매출입니다.");
        }
        this.issued = true;
    }

    public void cancelIssue() {
        if (!issued) {
            throw new BusinessException("발행되지 않은 매출입니다.");
        }
        this.issued = false;
    }

    public Long getProjectId() {
        return projectId;
    }

    public int getSequence() {
        return sequence;
    }

    public LocalDate getRevenueDate() {
        return revenueDate;
    }

    public RevenueType getType() {
        return type;
    }

    public Money getAmount() {
        return amount;
    }

    public boolean isIssued() {
        return issued;
    }

    public @Nullable String getMemo() {
        return memo;
    }

    public record RevenuePlanInfo(int sequence, LocalDate revenueDate, RevenueType type, Money amount, @Nullable String memo) {
    }

    @Override
    public String auditLabel() {
        return "매출 계획";
    }

    @Override
    public String auditName() {
        return sequence + "차 " + type.label();
    }

    @Override
    public AuditRef auditParent() {
        return new AuditRef("Project", projectId);
    }

}
