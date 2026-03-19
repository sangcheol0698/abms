package kr.co.abacus.abms.domain.project;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

public record ProjectRevenuePlanUpdateRequest(
        Integer sequence,
        LocalDate revenueDate,
        RevenueType type,
        Long amount,
        @Nullable String memo) {

}
