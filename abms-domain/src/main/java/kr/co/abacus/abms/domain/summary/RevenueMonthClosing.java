package kr.co.abacus.abms.domain.summary;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.AbstractEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "tb_revenue_month_closing",
        uniqueConstraints = @UniqueConstraint(name = "UK_REVENUE_MONTH_CLOSING_TARGET_MONTH", columnNames = "target_month")
)
public class RevenueMonthClosing extends AbstractEntity {

    @Column(name = "target_month", nullable = false, comment = "마감대상월")
    private LocalDate targetMonth;

    @Enumerated(EnumType.STRING)
    @Column(name = "closing_status", nullable = false, length = 20, comment = "마감상태")
    private RevenueMonthClosingStatus status;

    @Nullable
    @Column(name = "closed_at", comment = "마감일시")
    private LocalDateTime closedAt;

    @Nullable
    @Column(name = "closed_by", comment = "마감자 계정 ID")
    private Long closedBy;

    public static RevenueMonthClosing close(LocalDate targetMonth, @Nullable Long closedBy) {
        RevenueMonthClosing closing = new RevenueMonthClosing();
        closing.targetMonth = YearMonth.from(Objects.requireNonNull(targetMonth)).atDay(1);
        closing.status = RevenueMonthClosingStatus.CLOSED;
        closing.closedAt = LocalDateTime.now();
        closing.closedBy = closedBy;
        return closing;
    }

    public boolean isClosed() {
        return this.status == RevenueMonthClosingStatus.CLOSED;
    }
}
