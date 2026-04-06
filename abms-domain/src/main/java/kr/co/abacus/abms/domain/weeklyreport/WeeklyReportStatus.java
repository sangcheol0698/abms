package kr.co.abacus.abms.domain.weeklyreport;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WeeklyReportStatus {

    PENDING("대기 중"),
    COLLECTING("데이터 집계 중"),
    GENERATING("AI 초안 작성 중"),
    DRAFT("초안"),
    CANCELLED("생성 중지"),
    FAILED("생성 실패");

    private final String description;
}
