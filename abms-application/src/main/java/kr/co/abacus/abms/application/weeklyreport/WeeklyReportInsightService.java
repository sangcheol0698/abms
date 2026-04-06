package kr.co.abacus.abms.application.weeklyreport;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import kr.co.abacus.abms.application.weeklyreport.dto.query.WeeklyReportInsightData;
import kr.co.abacus.abms.application.weeklyreport.dto.query.WeeklyReportSnapshot;

@Service
class WeeklyReportInsightService {

    WeeklyReportInsightData createInsights(WeeklyReportSnapshot snapshot) {
        List<String> highlights = new ArrayList<>();
        highlights.add("이번 주 신규 입사자는 %d명이며, 현재 재직 중 직원은 %d명입니다."
                .formatted(snapshot.employees().joinedThisWeek(), snapshot.employees().activeEmployees()));
        highlights.add("이번 주 시작된 프로젝트는 %d건, 종료된 프로젝트는 %d건입니다."
                .formatted(snapshot.projects().startedThisWeek(), snapshot.projects().endedThisWeek()));
        if (snapshot.revenue().monthlySummaryAvailable()) {
            highlights.add("%s 기준 월 매출 집계는 매출 %,d원 / 비용 %,d원 / 이익 %,d원입니다."
                    .formatted(
                            snapshot.revenue().targetMonth(),
                            snapshot.revenue().revenueAmount(),
                            snapshot.revenue().costAmount(),
                            snapshot.revenue().profitAmount()));
        } else {
            highlights.add("%s 기준 월 매출 집계는 아직 계산되지 않았습니다.".formatted(snapshot.revenue().targetMonth()));
        }

        List<String> risks = new ArrayList<>();
        if (!snapshot.risks().endingSoonProjectNames().isEmpty()) {
            risks.add("종료 임박 프로젝트: %s".formatted(String.join(", ", snapshot.risks().endingSoonProjectNames())));
        }
        if (!snapshot.risks().noAssignmentProjectNames().isEmpty()) {
            risks.add("현재 투입 인력이 확인되지 않는 프로젝트: %s"
                    .formatted(String.join(", ", snapshot.risks().noAssignmentProjectNames())));
        }
        if (!snapshot.risks().onLeaveDepartmentNames().isEmpty()) {
            risks.add("휴직 인원이 집중된 부서: %s".formatted(String.join(", ", snapshot.risks().onLeaveDepartmentNames())));
        }
        if (risks.isEmpty()) {
            risks.add("즉시 대응이 필요한 리스크 신호는 제한적으로 관찰되었습니다.");
        }

        List<String> nextActions = new ArrayList<>();
        nextActions.add("종료 임박 프로젝트의 마감 일정과 잔여 업무를 재점검합니다.");
        nextActions.add("투입 공백 가능성이 있는 프로젝트의 배정 현황을 확인합니다.");
        if (snapshot.employees().statusChangeTrackingLimited()) {
            nextActions.add("직원 상태 변경 이력은 별도 추적 테이블이 없어 세부 확인이 필요합니다.");
        } else {
            nextActions.add("직원 상태 변동 건에 대한 후속 조치를 확인합니다.");
        }

        return new WeeklyReportInsightData(highlights, risks, nextActions);
    }
}
