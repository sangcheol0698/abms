package kr.co.abacus.abms.application.weeklyreport;

import org.springframework.stereotype.Component;

import kr.co.abacus.abms.application.weeklyreport.dto.query.WeeklyReportInsightData;
import kr.co.abacus.abms.application.weeklyreport.dto.query.WeeklyReportSnapshot;

@Component
class WeeklyReportPromptFactory {

    static final String PROMPT_VERSION = "weekly-report-v1";

    private final WeeklyReportSnapshotJsonMapper snapshotJsonMapper;

    WeeklyReportPromptFactory(WeeklyReportSnapshotJsonMapper snapshotJsonMapper) {
        this.snapshotJsonMapper = snapshotJsonMapper;
    }

    String buildUserPrompt(WeeklyReportSnapshot snapshot, WeeklyReportInsightData insightData) {
        return """
                다음은 ABMS 주간 운영 보고서 작성을 위한 구조화 데이터입니다.

                작성 규칙:
                - 입력 데이터에 없는 수치나 사실을 절대 추가하지 마세요.
                - 아래 섹션 제목을 정확히 유지하세요.
                - 각 섹션은 2~5문장 또는 2~5개 불릿으로 간결하게 작성하세요.
                - 숫자는 입력 데이터 값을 그대로 사용하세요.
                - 확실하지 않거나 추적이 제한된 정보는 '확인 필요'라고 표기하세요.
                - 한국어로 작성하고 운영 총괄 보고 톤을 유지하세요.
                - 출력은 Markdown 본문만 반환하세요.

                고정 섹션:
                1. 이번 주 요약
                2. 인원/조직 변화
                3. 프로젝트 진행 현황
                4. 매출/계약 관점 포인트
                5. 리스크 및 이슈
                6. 다음 주 확인 사항
                7. 근거 데이터 요약

                사전 정리 인사이트:
                %s

                스냅샷 JSON:
                %s
                """.formatted(
                snapshotJsonMapper.toJson(insightData),
                snapshotJsonMapper.toJson(snapshot)
        );
    }
}
