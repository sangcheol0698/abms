package kr.co.abacus.abms.domain.project;

import java.time.LocalDate;


import org.jspecify.annotations.Nullable;


public record ProjectRevenuePlanCreateRequest(
        Long projectId,
        Integer sequence,
        LocalDate revenueDate,
        RevenueType type,
        Long amount,
        @Nullable String memo) {


}
