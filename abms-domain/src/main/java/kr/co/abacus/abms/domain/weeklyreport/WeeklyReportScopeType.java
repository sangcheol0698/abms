package kr.co.abacus.abms.domain.weeklyreport;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WeeklyReportScopeType {

    GLOBAL_OPS("운영 총괄");

    private final String description;
}
