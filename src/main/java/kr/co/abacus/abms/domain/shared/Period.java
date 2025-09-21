package kr.co.abacus.abms.domain.shared;

import java.time.LocalDate;

import jakarta.persistence.Embeddable;

import org.jspecify.annotations.Nullable;

@Embeddable
public record Period(LocalDate startDate, @Nullable LocalDate endDate) {

    public Period {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 이전일 수 없습니다: " + startDate + " ~ " + endDate);
        }
    }

}
