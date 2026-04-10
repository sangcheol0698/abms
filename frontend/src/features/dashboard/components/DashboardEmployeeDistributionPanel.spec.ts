import { ref } from 'vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { renderWithProviders } from '@/test-utils';
import DashboardEmployeeDistributionPanel from '@/features/dashboard/components/DashboardEmployeeDistributionPanel.vue';

const queryState = {
  data: ref<any>(null),
  isLoading: ref(false),
};

vi.mock('@/features/dashboard/queries/useDashboardQueries', () => ({
  useDashboardEmployeeOverviewQuery: () => ({
    data: queryState.data,
    isLoading: queryState.isLoading,
  }),
}));

async function mountPanel() {
  return renderWithProviders(DashboardEmployeeDistributionPanel, {
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

describe('DashboardEmployeeDistributionPanel', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    queryState.isLoading.value = false;
    queryState.data.value = {
      fullTimeCount: 6,
      freelancerCount: 2,
      outsourcingCount: 1,
      partTimeCount: 1,
    };
  });

  it('실데이터 기준으로 직원 구성 분포를 렌더링한다', async () => {
    const { wrapper } = await mountPanel();

    expect(wrapper.text()).toContain('정직원');
    expect(wrapper.text()).toContain('프리랜서');
    expect(wrapper.text()).toContain('외주');
    expect(wrapper.text()).toContain('파트타임');
    expect(wrapper.text()).toContain('10명');
  });

  it('데이터가 없으면 empty state를 표시한다', async () => {
    queryState.data.value = {
      fullTimeCount: 0,
      freelancerCount: 0,
      outsourcingCount: 0,
      partTimeCount: 0,
    };

    const { wrapper } = await mountPanel();

    expect(wrapper.text()).toContain('표시할 직원 구성 데이터가 없습니다.');
  });
});
