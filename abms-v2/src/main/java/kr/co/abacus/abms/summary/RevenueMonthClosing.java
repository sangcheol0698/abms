package kr.co.abacus.abms.summary;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.common.domain.BaseEntity;
import kr.co.abacus.abms.common.domain.BusinessException;

/**
 * 손익 집계 월 마감. 마감된 월은 자동/수동 재집계에서 제외된다.
 */
@Entity
@Table(name = "tb_revenue_month_closing")
public class RevenueMonthClosing extends BaseEntity {

    @Column(nullable = false, unique = true)
    private LocalDate targetMonth;

    @Column(nullable = false)
    private boolean closed;

    private @Nullable LocalDateTime closedAt;

    private @Nullable Long closedBy;

    protected RevenueMonthClosing() {
    }

    public static RevenueMonthClosing of(YearMonth month) {
        RevenueMonthClosing closing = new RevenueMonthClosing();
        closing.targetMonth = month.atDay(1);
        closing.closed = false;
        return closing;
    }

    public void close(Long accountId) {
        if (closed) {
            throw new BusinessException("이미 마감된 월입니다.");
        }
        this.closed = true;
        this.closedAt = LocalDateTime.now();
        this.closedBy = accountId;
    }

    public void reopen() {
        if (!closed) {
            throw new BusinessException("마감되지 않은 월입니다.");
        }
        this.closed = false;
        this.closedAt = null;
        this.closedBy = null;
    }

    public LocalDate getTargetMonth() {
        return targetMonth;
    }

    public boolean isClosed() {
        return closed;
    }

    public @Nullable LocalDateTime getClosedAt() {
        return closedAt;
    }

    public @Nullable Long getClosedBy() {
        return closedBy;
    }

}
