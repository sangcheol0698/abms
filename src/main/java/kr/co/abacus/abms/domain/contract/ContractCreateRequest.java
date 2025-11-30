package kr.co.abacus.abms.domain.contract;

import java.time.LocalDate;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

public record ContractCreateRequest(
    UUID projectId,
    @Nullable LocalDate contractDate,
    Long contractAmount,
    LocalDate startDate,
    @Nullable LocalDate endDate
) {
}
