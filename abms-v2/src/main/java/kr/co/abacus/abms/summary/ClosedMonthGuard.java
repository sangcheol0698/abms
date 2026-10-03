package kr.co.abacus.abms.summary;

import java.time.LocalDate;
import java.time.YearMonth;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.common.domain.BusinessException;

/**
 * 마감된 월의 손익에 영향을 주는 원천 데이터 변경을 막는다.
 * 마감 월 집계는 다시 계산되지 않으므로, 원천 데이터가 바뀌면 집계와 어긋나기 때문이다.
 */
@Component
@Transactional(readOnly = true)
public class ClosedMonthGuard {

    private static final LocalDate FAR_FUTURE = LocalDate.of(9999, 12, 1);

    private final RevenueMonthClosingRepository closingRepository;

    public ClosedMonthGuard(RevenueMonthClosingRepository closingRepository) {
        this.closingRepository = closingRepository;
    }

    /** 해당 날짜가 속한 월이 마감되었으면 막는다. */
    public void checkOpen(LocalDate date, String subject) {
        checkOpen(date, date, subject);
    }

    /** from ~ to(없으면 무기한) 사이에 마감된 월이 있으면 막는다. */
    public void checkOpen(LocalDate from, @Nullable LocalDate to, String subject) {
        LocalDate fromMonth = from.withDayOfMonth(1);
        LocalDate toMonth = to == null ? FAR_FUTURE : to.withDayOfMonth(1);
        if (fromMonth.isAfter(toMonth)) {
            return;
        }
        closingRepository.findFirstByClosedTrueAndTargetMonthBetweenOrderByTargetMonthAsc(fromMonth, toMonth)
                .ifPresent(closing -> {
                    throw new BusinessException(subject + ": " + YearMonth.from(closing.getTargetMonth())
                            + " 은(는) 마감된 월입니다. 마감 해제 후 변경하세요.");
                });
    }

}
