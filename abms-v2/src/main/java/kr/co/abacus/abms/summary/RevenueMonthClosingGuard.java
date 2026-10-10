package kr.co.abacus.abms.summary;

import java.time.LocalDate;
import java.time.YearMonth;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.ClosedMonthGuard;

/**
 * 월 마감 기록({@link RevenueMonthClosing})으로 {@link ClosedMonthGuard} 를 구현한다.
 */
@Component
@Transactional(readOnly = true)
public class RevenueMonthClosingGuard implements ClosedMonthGuard {

    private static final LocalDate FAR_FUTURE = LocalDate.of(9999, 12, 1);

    private final RevenueMonthClosingRepository closingRepository;

    public RevenueMonthClosingGuard(RevenueMonthClosingRepository closingRepository) {
        this.closingRepository = closingRepository;
    }

    @Override
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
