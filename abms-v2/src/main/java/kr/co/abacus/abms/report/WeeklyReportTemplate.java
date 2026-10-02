package kr.co.abacus.abms.report;

import kr.co.abacus.abms.common.domain.Money;
import kr.co.abacus.abms.report.WeeklySnapshot.AssignmentLine;
import kr.co.abacus.abms.report.WeeklySnapshot.ProjectLine;
import kr.co.abacus.abms.report.WeeklySnapshot.RevenueLine;
import kr.co.abacus.abms.summary.ProfitRow;

/**
 * 스냅샷을 마크다운 보고서로 변환한다. (AI 미설정 시 보고서 본문, AI 사용 시 프롬프트 데이터)
 */
final class WeeklyReportTemplate {

    private WeeklyReportTemplate() {
    }

    static String render(WeeklySnapshot s) {
        StringBuilder md = new StringBuilder();
        md.append("## 요약\n\n");
        md.append("- 기간: ").append(s.weekStart()).append(" ~ ").append(s.weekEnd()).append('\n');
        md.append("- 진행 프로젝트 ").append(s.activeProjects().size()).append("건, 금주 발행 매출 ")
                .append(s.issuedThisWeek().size()).append("건 (").append(won(s.issuedTotal())).append(")\n");
        if (!s.overdue().isEmpty()) {
            md.append("- 청구일이 지났지만 미발행인 매출 ").append(s.overdue().size()).append("건 확인 필요\n");
        }
        if (s.monthCalculated()) {
            md.append("- 당월 누적 손익: 매출 ").append(won(s.monthRevenue())).append(", 비용 ").append(won(s.monthCost()))
                    .append(", 이익 ").append(won(s.monthRevenue().minus(s.monthCost())))
                    .append(" (이익률 ").append(ProfitRow.margin(s.monthRevenue(), s.monthCost())).append("%)\n");
        }

        md.append("\n## 프로젝트 현황\n\n");
        if (s.activeProjects().isEmpty()) {
            md.append("진행 중인 프로젝트가 없습니다.\n");
        } else {
            md.append("| 코드 | 프로젝트 | 상태 | 고객사 | 주관 부서 | 기간 | 계약금액 | 투입 인원 |\n");
            md.append("|---|---|---|---|---|---|---:|---:|\n");
            for (ProjectLine p : s.activeProjects()) {
                md.append("| ").append(p.code()).append(" | ").append(p.name()).append(" | ").append(p.status())
                        .append(" | ").append(p.party()).append(" | ").append(p.department())
                        .append(" | ").append(p.startDate()).append(" ~ ").append(p.endDate())
                        .append(" | ").append(won(p.contractAmount())).append(" | ").append(p.memberCount()).append("명 |\n");
            }
        }

        md.append("\n## 매출/청구\n\n");
        md.append("### 금주 발행\n\n");
        revenueTable(md, s.issuedThisWeek(), "금주 발행된 매출이 없습니다.");
        md.append("\n### 향후 2주 청구 예정\n\n");
        revenueTable(md, s.upcoming(), "향후 2주 내 청구 예정 매출이 없습니다.");

        md.append("\n## 인력 투입 변화\n\n");
        if (s.assignmentChanges().isEmpty()) {
            md.append("금주 투입 시작/종료 인력이 없습니다.\n");
        } else {
            for (AssignmentLine a : s.assignmentChanges()) {
                md.append("- ").append(a.date()).append(" ").append(a.employeeName()).append(" — ")
                        .append(a.projectName()).append(" ").append(a.change()).append('\n');
            }
        }

        md.append("\n## 리스크 및 확인 필요 사항\n\n");
        if (s.overdue().isEmpty()) {
            md.append("- 특이 사항 없음\n");
        } else {
            for (RevenueLine r : s.overdue()) {
                md.append("- [미발행] ").append(r.projectName()).append(" ").append(r.sequence()).append("차 ")
                        .append(r.type()).append(" (청구일 ").append(r.date()).append(", ").append(won(r.amount())).append(")\n");
            }
        }
        return md.toString();
    }

    private static void revenueTable(StringBuilder md, java.util.List<RevenueLine> lines, String empty) {
        if (lines.isEmpty()) {
            md.append(empty).append('\n');
            return;
        }
        md.append("| 프로젝트 | 차수 | 유형 | 청구일 | 금액 |\n|---|---:|---|---|---:|\n");
        for (RevenueLine r : lines) {
            md.append("| ").append(r.projectName()).append(" | ").append(r.sequence()).append(" | ").append(r.type())
                    .append(" | ").append(r.date()).append(" | ").append(won(r.amount())).append(" |\n");
        }
    }

    private static String won(Money money) {
        return money.formatted() + "원";
    }

}
