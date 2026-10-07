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
 * 프로젝트 직접비 (외주 용역비, 장비·자재, 라이선스, 출장비 등 인건비 외 비용).
 * <p>
 * 귀속일이 속한 월의 프로젝트 비용으로 집계한다. 금액은 공급가액(부가세 제외) 기준이다.
 */
@Entity
@Table(name = "tb_project_expense")
@SQLRestriction("deleted = false")
public class ProjectExpense extends BaseEntity implements Auditable {

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private LocalDate expenseDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ExpenseCategory category;

    @Column(nullable = false)
    private Money amount;

    @Column(nullable = false, length = 100)
    private String description;

    private @Nullable String memo;

    protected ProjectExpense() {
    }

    /** 귀속일은 프로젝트 시작일부터 종료일 이후 이 개월 수까지 (마무리·하자보수 비용) */
    public static final int MONTHS_AFTER_END = 3;

    public static ProjectExpense create(Project project, ExpenseInfo info) {
        checkWithinProject(project, info.expenseDate());
        ProjectExpense expense = new ProjectExpense();
        expense.projectId = Objects.requireNonNull(project.id());
        expense.apply(info);
        return expense;
    }

    /** 귀속일을 바꿀 때만 기간을 확인한다. (나중에 프로젝트 기간이 바뀌어도 분류·메모 수정은 막지 않는다) */
    public void update(Project project, ExpenseInfo info) {
        if (!info.expenseDate().equals(expenseDate)) {
            checkWithinProject(project, info.expenseDate());
        }
        apply(info);
    }

    /** 귀속일로 입력할 수 있는 마지막 날 */
    public static LocalDate lastDateOf(Project project) {
        LocalDate end = project.getPeriod().endDate();
        return end == null ? LocalDate.MAX : end.plusMonths(MONTHS_AFTER_END);
    }

    private static void checkWithinProject(Project project, @Nullable LocalDate date) {
        Objects.requireNonNull(date, "귀속일은 필수입니다.");
        LocalDate first = project.getPeriod().startDate();
        LocalDate last = lastDateOf(project);
        if (date.isBefore(first) || date.isAfter(last)) {
            throw new BusinessException("귀속일은 프로젝트 시작일부터 종료 " + MONTHS_AFTER_END + "개월 후까지(" + first + " ~ "
                    + (last.equals(LocalDate.MAX) ? "" : last) + ") 입력할 수 있습니다: " + date);
        }
    }

    private void apply(ExpenseInfo info) {
        if (info.amount().isNegative() || info.amount().equals(Money.ZERO)) {
            throw new BusinessException("직접비 금액은 0원보다 커야 합니다.");
        }
        if (info.description() == null || info.description().isBlank()) {
            throw new BusinessException("직접비 내용을 입력하세요.");
        }
        this.expenseDate = Objects.requireNonNull(info.expenseDate(), "귀속일은 필수입니다.");
        this.category = Objects.requireNonNull(info.category());
        this.amount = info.amount();
        this.description = info.description().trim();
        this.memo = info.memo() == null || info.memo().isBlank() ? null : info.memo().trim();
    }

    public Long getProjectId() {
        return projectId;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public Money getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public @Nullable String getMemo() {
        return memo;
    }

    public record ExpenseInfo(LocalDate expenseDate, ExpenseCategory category, Money amount, String description, @Nullable String memo) {
    }

    @Override
    public String auditLabel() {
        return "직접비";
    }

    @Override
    public String auditName() {
        return category.label() + " · " + description;
    }

    @Override
    public AuditRef auditParent() {
        return new AuditRef("Project", projectId);
    }

}
