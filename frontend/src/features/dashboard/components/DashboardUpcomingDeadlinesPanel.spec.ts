import { ref } from 'vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { renderWithProviders } from '@/test-utils';
import DashboardUpcomingDeadlinesPanel from '@/features/dashboard/components/DashboardUpcomingDeadlinesPanel.vue';

const queryState = {
  data: ref<any>(null),
  isLoading: ref(false),
};

vi.mock('@/features/dashboard/queries/useDashboardQueries', () => ({
  useDashboardUpcomingDeadlinesQuery: () => ({
    data: queryState.data,
    isLoading: queryState.isLoading,
  }),
}));

async function mountPanel() {
  return renderWithProviders(DashboardUpcomingDeadlinesPanel, {
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

describe('DashboardUpcomingDeadlinesPanel', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    queryState.isLoading.value = false;
    queryState.data.value = [
      {
        projectId: 1,
        name: '지연 프로젝트',
        partyName: '협력사 A',
        statusDescription: '진행 중',
        endDate: '2026-04-08',
        daysLeft: -2,
      },
      {
        projectId: 2,
        name: '오늘 마감 프로젝트',
        partyName: '협력사 B',
        statusDescription: '보류',
        endDate: '2026-04-10',
        daysLeft: 0,
      },
      {
        projectId: 3,
        name: '임박 프로젝트',
        partyName: '협력사 C',
        statusDescription: '진행 중',
        endDate: '2026-04-13',
        daysLeft: 3,
      },
    ];
  });

  it('지연, D-Day, D-N 표기를 렌더링한다', async () => {
    const { wrapper } = await mountPanel();

    expect(wrapper.text()).toContain('지연 2일');
    expect(wrapper.text()).toContain('D-Day');
    expect(wrapper.text()).toContain('D-3');
    expect(wrapper.text()).toContain('협력사 A');
  });

  it('대상 데이터가 없으면 empty state를 표시한다', async () => {
    queryState.data.value = [];

    const { wrapper } = await mountPanel();

    expect(wrapper.text()).toContain('30일 이내 마감 프로젝트가 없습니다.');
  });
});
