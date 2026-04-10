import { ref } from 'vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { renderWithProviders } from '@/test-utils';
import DashboardProjectStatusPanel from '@/features/dashboard/components/DashboardProjectStatusPanel.vue';

const queryState = {
  data: ref<any>(null),
  isLoading: ref(false),
};

vi.mock('@/features/dashboard/queries/useDashboardQueries', () => ({
  useDashboardProjectOverviewQuery: () => ({
    data: queryState.data,
    isLoading: queryState.isLoading,
  }),
}));

async function mountPanel() {
  return renderWithProviders(DashboardProjectStatusPanel, {
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

describe('DashboardProjectStatusPanel', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    queryState.isLoading.value = false;
    queryState.data.value = {
      totalCount: 10,
      scheduledCount: 1,
      inProgressCount: 4,
      completedCount: 3,
      onHoldCount: 2,
      cancelledCount: 0,
    };
  });

  it('프로젝트 상태 요약에서 건수와 비율을 계산한다', async () => {
    const { wrapper } = await mountPanel();

    expect(wrapper.text()).toContain('예약');
    expect(wrapper.text()).toContain('1건 (10%)');
    expect(wrapper.text()).toContain('진행 중');
    expect(wrapper.text()).toContain('4건 (40%)');
    expect(wrapper.text()).toContain('보류');
    expect(wrapper.text()).toContain('2건 (20%)');
  });

  it('프로젝트가 없으면 empty state를 표시한다', async () => {
    queryState.data.value = {
      totalCount: 0,
      scheduledCount: 0,
      inProgressCount: 0,
      completedCount: 0,
      onHoldCount: 0,
      cancelledCount: 0,
    };

    const { wrapper } = await mountPanel();

    expect(wrapper.text()).toContain('표시할 프로젝트 상태 데이터가 없습니다.');
  });
});
