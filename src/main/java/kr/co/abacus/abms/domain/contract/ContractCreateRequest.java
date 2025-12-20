package kr.co.abacus.abms.domain.contract;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

public record ContractCreateRequest(
    Long projectId,
    @Nullable LocalDate contractDate,
    Long contractAmount,
    LocalDate startDate,
    @Nullable LocalDate endDate) {

}
