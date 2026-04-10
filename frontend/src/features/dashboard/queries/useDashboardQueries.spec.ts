import { afterAll, beforeEach, describe, expect, it, vi } from 'vitest';
import { dashboardKeys } from '@/core/query';
import {
  useDashboardDepartmentFinancialQuery,
  useDashboardEmployeeOverviewQuery,
  useDashboardMonthlyRevenueSummaryQuery,
  useDashboardProjectOverviewQuery,
  useDashboardSummaryQuery,
  useDashboardUpcomingDeadlinesQuery,
} from '@/features/dashboard/queries/useDashboardQueries';

const vueQueryMocks = vi.hoisted(() => ({
  useQueryMock: vi.fn((options) => options),
}));

const dashboardRepositoryMock = {
  fetchSummary: vi.fn(),
  fetchUpcomingDeadlines: vi.fn(),
  fetchDepartmentFinancials: vi.fn(),
  fetchProjectOverview: vi.fn(),
  fetchEmployeeOverview: vi.fn(),
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
    resolve: (token: { name?: string }) => {
      switch (token?.name) {
        case 'DashboardRevenueRepository':
          return dashboardRevenueRepositoryMock;
        default:
          return dashboardRepositoryMock;
      }
    },
  },
}));

describe('useDashboardQueries', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-04-10T09:00:00+09:00'));
  });

  afterAll(() => {
    vi.useRealTimers();
  });

  it('대시보드 요약 query를 저장소와 연결한다', async () => {
    dashboardRepositoryMock.fetchSummary.mockResolvedValueOnce('summary');

    const query = useDashboardSummaryQuery(2026);

    expect(query.queryKey.value).toEqual(dashboardKeys.summary(2026));
    await expect(query.queryFn()).resolves.toBe('summary');
    expect(dashboardRepositoryMock.fetchSummary).toHaveBeenCalledWith(2026);
  });

  it('월별 매출 요약 query를 저장소와 연결한다', async () => {
    dashboardRevenueRepositoryMock.fetchMonthlyRevenueSummary.mockResolvedValueOnce('monthly');

    const query = useDashboardMonthlyRevenueSummaryQuery(2026);

    expect(query.queryKey.value).toEqual(dashboardKeys.monthlyRevenueSummary(2026));
    await expect(query.queryFn()).resolves.toBe('monthly');
    expect(dashboardRevenueRepositoryMock.fetchMonthlyRevenueSummary).toHaveBeenCalledWith(2026);
  });

  it('직원/프로젝트 overview query를 각 저장소와 연결한다', async () => {
    dashboardRepositoryMock.fetchEmployeeOverview.mockResolvedValueOnce('employees');
    dashboardRepositoryMock.fetchProjectOverview.mockResolvedValueOnce('projects');

    const employeeQuery = useDashboardEmployeeOverviewQuery(2026);
    const projectQuery = useDashboardProjectOverviewQuery(2026);

    expect(employeeQuery.queryKey.value).toEqual(dashboardKeys.employeeOverview(2026));
    expect(projectQuery.queryKey.value).toEqual(dashboardKeys.projectOverview(2026));
    await expect(employeeQuery.queryFn()).resolves.toBe('employees');
    await expect(projectQuery.queryFn()).resolves.toBe('projects');
    expect(dashboardRepositoryMock.fetchEmployeeOverview).toHaveBeenCalledWith(2026);
    expect(dashboardRepositoryMock.fetchProjectOverview).toHaveBeenCalledWith(2026);
  });

  it('대시보드 신규 패널 query를 저장소와 연결한다', async () => {
    dashboardRepositoryMock.fetchUpcomingDeadlines.mockResolvedValueOnce('deadlines');
    dashboardRepositoryMock.fetchDepartmentFinancials.mockResolvedValueOnce('financials');

    const upcomingQuery = useDashboardUpcomingDeadlinesQuery();
    const financialQuery = useDashboardDepartmentFinancialQuery(2026);

    expect(upcomingQuery.queryKey).toEqual(dashboardKeys.upcomingDeadlines(5));
    expect(financialQuery.queryKey.value).toEqual(dashboardKeys.departmentFinancials(2026, 5));
    await expect(upcomingQuery.queryFn()).resolves.toBe('deadlines');
    await expect(financialQuery.queryFn()).resolves.toBe('financials');
    expect(dashboardRepositoryMock.fetchUpcomingDeadlines).toHaveBeenCalledWith(5);
    expect(dashboardRepositoryMock.fetchDepartmentFinancials).toHaveBeenCalledWith(2026, 5);
  });
});
