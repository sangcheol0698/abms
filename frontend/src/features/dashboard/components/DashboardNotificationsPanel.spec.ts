import { describe, expect, it } from 'vitest';
import { renderWithProviders } from '@/test-utils';
import DashboardNotificationsPanel from '@/features/dashboard/components/DashboardNotificationsPanel.vue';

async function mountPanel(props: Record<string, unknown>) {
  return renderWithProviders(DashboardNotificationsPanel, {
    props,
    global: {
      stubs: {
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

describe('DashboardNotificationsPanel', () => {
  it('알림과 unread badge를 렌더링한다', async () => {
    const { wrapper } = await mountPanel({
      notifications: [
        {
          id: 1,
          title: '알림 1',
          description: '설명 1',
          createdAt: '2026-04-10T00:00:00Z',
          read: false,
          type: 'info',
        },
      ],
      unreadCount: 3,
      isLoading: false,
    });

    expect(wrapper.text()).toContain('알림 1');
    expect(wrapper.text()).toContain('3');
  });

  it('loading 및 empty state를 처리한다', async () => {
    const loading = await mountPanel({
      notifications: [],
      unreadCount: 0,
      isLoading: true,
    });
    expect(loading.wrapper.text()).toContain('알림을 불러오는 중입니다...');

    const empty = await mountPanel({
      notifications: [],
      unreadCount: 0,
      isLoading: false,
    });
    expect(empty.wrapper.text()).toContain('최근 알림이 없습니다.');
  });
});
