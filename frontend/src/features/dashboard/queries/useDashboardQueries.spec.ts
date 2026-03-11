import { beforeEach, describe, expect, it, vi } from 'vitest';
import { dashboardKeys } from '@/core/query';
import {
  useDashboardMonthlyRevenueSummaryQuery,
  useDashboardSummaryQuery,
} from '@/features/dashboard/queries/useDashboardQueries';

const vueQueryMocks = vi.hoisted(() => ({
  useQueryMock: vi.fn((options) => options),
}));

const dashboardRepositoryMock = {
  fetchSummary: vi.fn(),
};

const dashboardRevenueRepositoryMock = {
  fetchMonthlyRevenueSummary: vi.fn(),
};

vi.mock('@tanstack/vue-query', async () => {
  const actual = await vi.importActual<typeof import('@tanstack/vue-query')>('@tanstack/vue-query');
  return {
    ...actual,
    useQuery: vueQueryMocks.useQueryMock,
  };
});

vi.mock('@/core/di/container', () => ({
  appContainer: {
    resolve: (token: { name?: string }) =>
      token?.name === 'DashboardRevenueRepository'
        ? dashboardRevenueRepositoryMock
        : dashboardRepositoryMock,
  },
}));

describe('useDashboardQueries', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('대시보드 요약 query를 저장소와 연결한다', async () => {
    dashboardRepositoryMock.fetchSummary.mockResolvedValueOnce('summary');

    const query = useDashboardSummaryQuery();

    expect(query.queryKey).toEqual(dashboardKeys.summary());
    await expect(query.queryFn()).resolves.toBe('summary');
  });

  it('월별 매출 요약 query를 저장소와 연결한다', async () => {
    dashboardRevenueRepositoryMock.fetchMonthlyRevenueSummary.mockResolvedValueOnce('monthly');

    const query = useDashboardMonthlyRevenueSummaryQuery();

    expect(query.queryKey).toEqual(dashboardKeys.monthlyRevenueSummary());
    await expect(query.queryFn()).resolves.toBe('monthly');
  });
});
