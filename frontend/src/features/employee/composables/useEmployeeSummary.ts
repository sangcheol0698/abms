import { computed, unref, type MaybeRef } from 'vue';
import type { EmployeeOverviewSummary } from '@/features/employee/repository/EmployeeRepository';

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

interface UseEmployeeSummaryParams {
  summary: MaybeRef<EmployeeOverviewSummary | null | undefined>;
}

export function useEmployeeSummary(params: UseEmployeeSummaryParams) {
  return computed(() => {
    const summary = unref(params.summary);
    const total = summary?.totalCount ?? 0;
    const activeCount = summary?.activeCount ?? 0;
    const onLeaveCount = summary?.onLeaveCount ?? 0;
    const fullTimeCount = summary?.fullTimeCount ?? 0;
    const freelancerCount = summary?.freelancerCount ?? 0;
    const outsourcingCount = summary?.outsourcingCount ?? 0;
    const partTimeCount = summary?.partTimeCount ?? 0;

    const cards: EmployeeSummaryCard[] = [
      {
        id: 'total',
        title: '총 직원',
        value: `${total.toLocaleString()}명`,
        description: '현재 필터 기준 직원 수',
      },
      {
        id: 'active',
        title: '근무 중',
        value: `${activeCount.toLocaleString()}명`,
        description: total === 0 ? '데이터 없음' : `전체의 ${Math.round((activeCount / total) * 100)}%`,
      },
      {
        id: 'leave',
        title: '휴가·휴직',
        value: `${onLeaveCount.toLocaleString()}명`,
        description:
          onLeaveCount === 0
            ? '휴가 중 직원이 없습니다'
            : total === 0
              ? '데이터 없음'
              : `전체의 ${Math.round((onLeaveCount / total) * 100)}%`,
      },
      {
        id: 'type',
        title: '직원 유형 분포',
        value: total === 0 ? '데이터 없음' : `정직원 ${fullTimeCount.toLocaleString()}명`,
        description:
          total === 0
            ? '데이터 없음'
            : `프리랜서 ${freelancerCount} · 외주 ${outsourcingCount} · 반프리 ${partTimeCount}`,
      },
    ];

    return { cards };
  });
}
