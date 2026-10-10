package kr.co.abacus.abms.common.domain;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

/**
 * 마감된 월의 손익에 영향을 주는 원천 데이터 변경을 막는다.
 * 마감 월 집계는 다시 계산되지 않으므로, 원천 데이터가 바뀌면 집계와 어긋나기 때문이다.
 * 마감은 손익(summary)이 관리하지만, 원천 데이터를 가진 직원·프로젝트가 손익을 직접 알지 않도록 여기에 계약만 둔다.
 */
public interface ClosedMonthGuard {

    /** 해당 날짜가 속한 월이 마감되었으면 막는다. */
    default void checkOpen(LocalDate date, String subject) {
        checkOpen(date, date, subject);
    }

    /** from ~ to(없으면 무기한) 사이에 마감된 월이 있으면 {@link BusinessException} 으로 막는다. */
    void checkOpen(LocalDate from, @Nullable LocalDate to, String subject);

}
