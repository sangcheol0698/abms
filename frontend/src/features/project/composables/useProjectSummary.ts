import { computed, unref, type MaybeRef } from 'vue';
import type { ProjectOverviewSummary } from '@/features/project/repository/ProjectRepository';
import { formatCurrency } from '@/features/project/models/projectListItem';

export interface ProjectSummaryCard {
  id: string;
  title: string;
  value: string;
  description?: string;
  trend?: {
    label: string;
    direction: 'up' | 'down' | 'flat';
  };
}

interface UseProjectSummaryParams {
  summary: MaybeRef<ProjectOverviewSummary | null | undefined>;
}

export function useProjectSummary(params: UseProjectSummaryParams) {
  return computed(() => {
    const summary = unref(params.summary);
    const cards: ProjectSummaryCard[] = [
      {
        id: 'total',
        title: '총 프로젝트',
        value: `${(summary?.totalCount ?? 0).toLocaleString()}건`,
        description: '현재 필터 기준 프로젝트 수',
      },
      {
        id: 'in-progress',
        title: '진행 중',
        value: `${(summary?.inProgressCount ?? 0).toLocaleString()}건`,
        description: `예약 ${(summary?.scheduledCount ?? 0).toLocaleString()}건`,
      },
      {
        id: 'completed',
        title: '완료',
        value: `${(summary?.completedCount ?? 0).toLocaleString()}건`,
        description: `보류 ${(summary?.onHoldCount ?? 0).toLocaleString()}건 · 취소 ${(summary?.cancelledCount ?? 0).toLocaleString()}건`,
      },
      {
        id: 'contract',
        title: '총 계약금액',
        value: formatCurrency(summary?.totalContractAmount ?? 0),
        description: '현재 필터 기준 계약 금액 합계',
      },
    ];

    return { cards };
  });
}
