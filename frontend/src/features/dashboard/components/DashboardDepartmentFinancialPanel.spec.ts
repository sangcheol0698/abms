import { ref } from 'vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { renderWithProviders } from '@/test-utils';
import DashboardDepartmentFinancialPanel from '@/features/dashboard/components/DashboardDepartmentFinancialPanel.vue';

const queryState = {
  data: ref<any>(null),
  isLoading: ref(false),
};

vi.mock('@/features/dashboard/queries/useDashboardQueries', () => ({
  useDashboardDepartmentFinancialQuery: () => ({
    data: queryState.data,
    isLoading: queryState.isLoading,
  }),
}));

async function mountPanel() {
  return renderWithProviders(DashboardDepartmentFinancialPanel, {
    global: {
      stubs: {
        Card: { template: '<div><slot /></div>' },
        CardHeader: { template: '<div><slot /></div>' },
        CardContent: { template: '<div><slot /></div>' },
        CardDescription: { template: '<div><slot /></div>' },
        CardTitle: { template: '<div><slot /></div>' },
      },
    },
  });
}

describe('DashboardDepartmentFinancialPanel', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    queryState.isLoading.value = false;
    queryState.data.value = [
      {
        departmentId: 1,
        departmentName: '알파팀',
        revenue: 220000000,
        profit: 75000000,
        profitMargin: 34.1,
      },
    ];
  });

  it('부서별 재무 데이터를 렌더링한다', async () => {
    const { wrapper } = await mountPanel();

    expect(wrapper.text()).toContain('알파팀');
    expect(wrapper.text()).toContain('220,000,000원');
    expect(wrapper.text()).toContain('이익 75,000,000원');
    expect(wrapper.text()).toContain('이익률 34.1%');
  });

  it('로딩 및 empty state를 처리한다', async () => {
    queryState.isLoading.value = true;
    queryState.data.value = [];

    const loading = await mountPanel();
    expect(loading.wrapper.text()).toContain('데이터를 불러오는 중입니다...');

    queryState.isLoading.value = false;
    const empty = await mountPanel();
    expect(empty.wrapper.text()).toContain('표시할 부서별 재무 데이터가 없습니다.');
  });
});
