import { computed, unref, type MaybeRef } from 'vue';
import type { EmployeeListItem } from '@/features/employee/models/employeeListItem';

export interface EmployeeSummaryCard {
  id: string;
  title: string;
  value: string;
  description?: string;
  trend?: {
    label: string;
    direction: 'up' | 'down' | 'flat';
  };
}

export interface EmployeeSummaryInsight {
  headline: string;
  subline?: string;
}

interface UseEmployeeSummaryParams {
  employees: MaybeRef<EmployeeListItem[]>;
  previousEmployees?: MaybeRef<EmployeeListItem[]>;
}

function countBy<T extends string | number | symbol>(items: EmployeeListItem[], accessor: (item: EmployeeListItem) => T) {
  return items.reduce<Record<T, number>>((acc, item) => {
    const key = accessor(item);
    acc[key] = (acc[key] ?? 0) + 1;
    return acc;
  }, {} as Record<T, number>);
}

export function useEmployeeSummary(params: UseEmployeeSummaryParams) {
  return computed(() => {
    const employees = unref(params.employees) ?? [];
    const previousEmployees = unref(params.previousEmployees) ?? [];
    const total = employees.length;
    const previousTotal = previousEmployees.length;

    const statusCounts = countBy(employees, (item) => item.statusCode);
    const typeCounts = countBy(employees, (item) => item.typeCode);

    const activeCount = statusCounts.working ?? statusCounts.active ?? statusCounts.ACTIVE ?? 0;
    const onLeaveCount =
      statusCounts.leave ?? statusCounts.onleave ?? statusCounts.ON_LEAVE ?? statusCounts.PLANNED_LEAVE ?? 0;
    const resignedCount = statusCounts.resigned ?? statusCounts.exit ?? statusCounts.RESIGNED ?? 0;

    const regularCount = typeCounts.regular ?? typeCounts.fulltime ?? typeCounts.FULL_TIME ?? 0;
    const contractCount = typeCounts.contract ?? typeCounts.parttime ?? typeCounts.CONTRACT ?? 0;

    const totalDelta = total - previousTotal;

    const activeRatio = total === 0 ? 0 : Math.round((activeCount / total) * 100);
    const leaveRatio = total === 0 ? 0 : Math.round((onLeaveCount / total) * 100);
    const regularRatio = total === 0 ? 0 : Math.round((regularCount / total) * 100);
    const contractRatio = total === 0 ? 0 : Math.round((contractCount / total) * 100);

    const cards: EmployeeSummaryCard[] = [
      {
        id: 'total',
        title: '총 구성원',
        value: `${total.toLocaleString()}명`,
        description: '현재 테이블 기준 구성원 수',
        trend: {
          label:
            totalDelta > 0
              ? `+${totalDelta}명 지난 기간 대비 증가`
              : totalDelta < 0
              ? `${totalDelta}명 감소`
              : '변화 없음',
          direction: totalDelta > 0 ? 'up' : totalDelta < 0 ? 'down' : 'flat',
        },
      },
      {
        id: 'active',
        title: '근무 중',
        value: `${activeCount.toLocaleString()}명`,
        description:
          total === 0 ? '데이터 없음' : `전체의 ${activeRatio}%`,
      },
      {
        id: 'leave',
        title: '휴가·휴직',
        value: `${onLeaveCount.toLocaleString()}명`,
        description:
          onLeaveCount === 0
            ? '휴가 중 구성원이 없습니다'
            : total === 0
            ? '데이터 없음'
            : `전체의 ${leaveRatio}%`,
      },
      {
        id: 'type',
        title: '정규/계약 비율',
        value:
          total === 0
            ? '데이터 없음'
            : `${regularRatio}% 정규 / ${contractRatio}% 계약`,
        description: '근무 유형 분포',
      },
    ];

    const insights: EmployeeSummaryInsight[] = [];

    if (onLeaveCount > 0) {
      insights.push({
        headline: `휴가 중인 구성원이 ${onLeaveCount}명입니다`,
        subline: '업무 공백을 대비한 승계 계획을 확인하세요.',
      });
    }

    if (resignedCount > 0) {
      insights.push({
        headline: `최근 퇴사 처리된 구성원이 ${resignedCount}명 있습니다`,
        subline: '퇴사 사유와 대체 인력 검토가 필요합니다.',
      });
    }

    if (insights.length === 0) {
      insights.push({
        headline: '구성원 상태가 안정적입니다',
        subline: '필요 시 신규 채용 계획을 검토해 보세요.',
      });
    }

    return { cards, insights };
  });
}
