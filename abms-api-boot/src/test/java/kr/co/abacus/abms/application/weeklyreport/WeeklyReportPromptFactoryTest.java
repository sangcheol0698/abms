package kr.co.abacus.abms.application.weeklyreport;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;

import kr.co.abacus.abms.application.weeklyreport.dto.query.WeeklyReportInsightData;
import kr.co.abacus.abms.application.weeklyreport.dto.query.WeeklyReportSnapshot;

@DisplayName("주간 보고서 프롬프트 팩토리")
class WeeklyReportPromptFactoryTest {

    @Test
    @DisplayName("프롬프트에 고정 섹션과 인사이트, 스냅샷 JSON을 포함한다")
    void buildPromptContainsSectionsAndSnapshot() {
        WeeklyReportSnapshot snapshot = new WeeklyReportSnapshot(
                LocalDate.of(2026, 3, 23),
                LocalDate.of(2026, 3, 29),
                new WeeklyReportSnapshot.EmployeeSection(
                        10, 8, 1, 1, 3, 2, 1, true,
                        List.of(new WeeklyReportSnapshot.DepartmentOnLeave(1L, "플랫폼팀", 1))
                ),
                new WeeklyReportSnapshot.ProjectSection(
                        4, 2, 1, 1, 1, 100_000L, 50_000L, 2,
                        List.of(new WeeklyReportSnapshot.ProjectDeadlineItem(1L, "ABMS 고도화", LocalDate.of(2026, 4, 2))),
                        List.of(new WeeklyReportSnapshot.ProjectCoverageItem(2L, "인력 공백 프로젝트"))
                ),
                new WeeklyReportSnapshot.RevenueSection(true, "202603", 1_000_000L, 500_000L, 500_000L),
                new WeeklyReportSnapshot.RiskSection(
                        List.of("ABMS 고도화"),
                        List.of("인력 공백 프로젝트"),
                        List.of("플랫폼팀")
                )
        );
        WeeklyReportInsightData insightData = new WeeklyReportInsightData(
                List.of("신규 입사 2명"),
                List.of("종료 임박 프로젝트 1건"),
                List.of("프로젝트 마감 일정 점검")
        );

        WeeklyReportPromptFactory promptFactory = new WeeklyReportPromptFactory(
                new WeeklyReportSnapshotJsonMapper(new ObjectMapper())
        );

        String prompt = promptFactory.buildUserPrompt(snapshot, insightData);

        assertThat(prompt).contains("1. 이번 주 요약");
        assertThat(prompt).contains("7. 근거 데이터 요약");
        assertThat(prompt).contains("신규 입사 2명");
        assertThat(prompt).contains("\"weekStart\" : \"2026-03-23\"");
        assertThat(prompt).contains("\"targetMonth\" : \"202603\"");
    }
}
