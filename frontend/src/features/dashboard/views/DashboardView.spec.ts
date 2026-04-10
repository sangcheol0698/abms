import { ref } from 'vue';
import { afterAll, beforeEach, describe, expect, it, vi } from 'vitest';
import { renderWithProviders } from '@/test-utils';
import DashboardView from '@/features/dashboard/views/DashboardView.vue';

const dashboardSummaryQueryState = {
  data: ref<any>(null),
  isLoading: ref(false),
};

vi.mock('@/features/dashboard/queries/useDashboardQueries', () => ({
  useDashboardSummaryQuery: () => ({
    data: dashboardSummaryQueryState.data,
    isLoading: dashboardSummaryQueryState.isLoading,
  }),
}));

async function mountDashboardView() {
  return renderWithProviders(DashboardView, {
    route: '/',
    global: {
      stubs: {
        DashboardMonthlyFinancialPanel: true,
        DashboardDepartmentFinancialPanel: true,
        DashboardProjectStatusPanel: true,
        DashboardUpcomingDeadlinesPanel: true,
        DashboardEmployeeDistributionPanel: true,
        DashboardSummaryCards: {
          props: ['summary', 'isLoading', 'selectedYear'],
          template:
            '<div data-test="summary-cards">{{ isLoading ? "loading" : "ready" }}::{{ selectedYear }}</div>',
        },
        Select: { template: '<div><slot /></div>' },
        SelectTrigger: { template: '<button><slot /></button>' },
        SelectValue: { template: '<span><slot /></span>' },
        SelectContent: { template: '<div><slot /></div>' },
        SelectItem: { template: '<div><slot /></div>' },
      },
    },
  });
}

describe('DashboardView', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-04-10T09:00:00+09:00'));
    dashboardSummaryQueryState.data.value = {
      totalEmployeesCount: 10,
      activeProjectsCount: 4,
      completedProjectsCount: 3,
      newEmployeesCount: 1,
      yearRevenue: 250000000,
      yearProfit: 90000000,
    };
    dashboardSummaryQueryState.isLoading.value = false;
  });

  afterAll(() => {
    vi.useRealTimers();
  });

  it('핵심 패널을 렌더링한다', async () => {
    const { wrapper } = await mountDashboardView();

    expect(wrapper.find('[data-test="summary-cards"]').exists()).toBe(true);
    expect(wrapper.find('[data-test="summary-cards"]').text()).toContain('::2026');
  });

  it('요약 query가 로딩 중이면 KPI 카드에 loading 상태를 전달한다', async () => {
    dashboardSummaryQueryState.isLoading.value = true;

    const { wrapper } = await mountDashboardView();

    expect(wrapper.find('[data-test="summary-cards"]').text()).toContain('loading');
  });
});
