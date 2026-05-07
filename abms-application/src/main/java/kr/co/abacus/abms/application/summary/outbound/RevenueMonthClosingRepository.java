package kr.co.abacus.abms.application.summary.outbound;

import java.time.LocalDate;

import kr.co.abacus.abms.domain.summary.RevenueMonthClosingStatus;

public interface RevenueMonthClosingRepository {

    boolean existsByTargetMonthAndStatusAndDeletedFalse(LocalDate targetMonth, RevenueMonthClosingStatus status);
}
