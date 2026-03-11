import { computed, ref } from 'vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { renderWithProviders, createMockQueryState } from '@/test-utils';
import DashboardView from '@/features/dashboard/views/DashboardView.vue';

const notificationsRef = ref<
  Array<{
    id: number;
    title: string;
    description?: string;
    createdAt: string;
    read: boolean;
    type: 'info' | 'warning' | 'success' | 'error';
  }>
>([]);
const fetchAllMock = vi.fn();
const dashboardSummaryQueryState = {
  data: ref<any>(null),
  isLoading: ref(false),
};

vi.mock('pinia', async () => {
  const actual = await vi.importActual<typeof import('pinia')>('pinia');
  return {
    ...actual,
    storeToRefs: () => ({
      sorted: computed(() => notificationsRef.value),
    }),
  };
});

vi.mock('@/core/stores/notifications.store', () => ({
  useNotificationsStore: () => ({
    fetchAll: fetchAllMock,
  }),
}));

vi.mock('@/features/dashboard/queries/useDashboardQueries', () => ({
  useDashboardSummaryQuery: () => ({
    data: dashboardSummaryQueryState.data,
    isLoading: dashboardSummaryQueryState.isLoading,
  }),
  useDashboardMonthlyRevenueSummaryQuery: () => createMockQueryState({ data: [] }),
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
        DashboardEmployeeStatusPanel: true,
        DashboardJobLevelPanel: true,
        Card: { template: '<div><slot /></div>' },
        CardHeader: { template: '<div><slot /></div>' },
        CardContent: { template: '<div><slot /></div>' },
        CardDescription: { template: '<div><slot /></div>' },
        CardTitle: { template: '<div><slot /></div>' },
        Badge: { template: '<span><slot /></span>' },
      },
    },
  });
}

describe('DashboardView', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    dashboardSummaryQueryState.data.value = {
      totalEmployeesCount: 10,
      onLeaveEmployeesCount: 1,
    };
    dashboardSummaryQueryState.isLoading.value = false;
    notificationsRef.value = [];
  });

  it('마운트 시 알림을 조회하고, 알림이 없으면 빈 상태를 표시한다', async () => {
    const { wrapper } = await mountDashboardView();

    expect(fetchAllMock).toHaveBeenCalled();
    expect(wrapper.text()).toContain('최근 알림이 없습니다.');
  });

  it('로딩 중에는 스켈레톤을 렌더링한다', async () => {
    dashboardSummaryQueryState.isLoading.value = true;

    const { wrapper } = await mountDashboardView();

    expect(wrapper.html()).toContain('animate-pulse');
  });

  it('최근 알림은 최대 5개까지만 표시한다', async () => {
    notificationsRef.value = Array.from({ length: 6 }, (_, index) => ({
      id: index + 1,
      title: `알림 ${index + 1}`,
      description: `설명 ${index + 1}`,
      createdAt: `2024-01-0${Math.min(index + 1, 9)}T00:00:00Z`,
      read: false,
      type: 'info',
    }));

    const { wrapper } = await mountDashboardView();

    expect(wrapper.text()).toContain('알림 1');
    expect(wrapper.text()).toContain('알림 5');
    expect(wrapper.text()).not.toContain('알림 6');
  });
});
