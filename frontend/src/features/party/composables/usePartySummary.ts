import { computed, unref, type MaybeRef } from 'vue';
import type { PartyOverviewSummary } from '@/features/party/repository/PartyRepository';
import { formatCurrency } from '@/features/project/models/projectListItem';

export interface PartySummaryCard {
  id: string;
  title: string;
  value: string;
  description?: string;
  trend?: {
    label: string;
    direction: 'up' | 'down' | 'flat';
  };
}

interface UsePartySummaryParams {
  summary: MaybeRef<PartyOverviewSummary | null | undefined>;
}

export function usePartySummary(params: UsePartySummaryParams) {
  return computed(() => {
    const summary = unref(params.summary);
    const cards: PartySummaryCard[] = [
      {
        id: 'total',
        title: '총 협력사',
        value: `${(summary?.totalCount ?? 0).toLocaleString()}곳`,
        description: '현재 필터 기준 협력사 수',
      },
      {
        id: 'with-projects',
        title: '프로젝트 보유 협력사',
        value: `${(summary?.withProjectsCount ?? 0).toLocaleString()}곳`,
        description: `프로젝트 없는 협력사 ${(summary?.withoutProjectsCount ?? 0).toLocaleString()}곳`,
      },
      {
        id: 'with-in-progress',
        title: '진행 중 프로젝트 보유',
        value: `${(summary?.withInProgressProjectsCount ?? 0).toLocaleString()}곳`,
        description: '진행 중 프로젝트를 1건 이상 가진 협력사',
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
