import { computed, ref } from 'vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useNotificationsStore } from '@/core/stores/notifications.store';

const notificationsData = ref([
  {
    id: 1,
    title: '가장 오래된 알림',
    type: 'info' as const,
    createdAt: '2024-01-01T00:00:00Z',
    read: false,
  },
  {
    id: 2,
    title: '최근 알림',
    type: 'warning' as const,
    createdAt: '2024-01-02T00:00:00Z',
    read: true,
  },
]);

const refetchMock = vi.fn();
const markAsReadMutateAsyncMock = vi.fn();
const markAllMutateAsyncMock = vi.fn();
const createMutateAsyncMock = vi.fn();
const clearMutateAsyncMock = vi.fn();

vi.mock('@/features/notification/queries/useNotificationQueries', () => ({
  useNotificationsQuery: () => ({
    data: notificationsData,
    isFetching: ref(false),
    refetch: refetchMock,
  }),
  useMarkNotificationAsReadMutation: () => ({
    isPending: ref(false),
    mutateAsync: markAsReadMutateAsyncMock,
  }),
  useMarkAllNotificationsAsReadMutation: () => ({
    isPending: ref(false),
    mutateAsync: markAllMutateAsyncMock,
  }),
  useCreateNotificationMutation: () => ({
    isPending: ref(false),
    mutateAsync: createMutateAsyncMock,
  }),
  useClearNotificationsMutation: () => ({
    isPending: ref(false),
    mutateAsync: clearMutateAsyncMock,
  }),
}));

describe('notifications.store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
    notificationsData.value = [
      {
        id: 1,
        title: '가장 오래된 알림',
        type: 'info',
        createdAt: '2024-01-01T00:00:00Z',
        read: false,
      },
      {
        id: 2,
        title: '최근 알림',
        type: 'warning',
        createdAt: '2024-01-02T00:00:00Z',
        read: true,
      },
    ];
  });

  it('정렬, 미확인 개수, fetchAll을 제공한다', async () => {
    const store = useNotificationsStore();

    expect(store.unreadCount).toBe(1);
    expect(store.sorted.map((item) => item.id)).toEqual([2, 1]);

    await store.fetchAll();

    expect(refetchMock).toHaveBeenCalled();
  });

  it('이미 읽은 알림은 markAsRead를 건너뛴다', async () => {
    const store = useNotificationsStore();

    await store.markAsRead(2);
    await store.markAsRead(1);

    expect(markAsReadMutateAsyncMock).toHaveBeenCalledTimes(1);
    expect(markAsReadMutateAsyncMock).toHaveBeenCalledWith(1);
  });

  it('읽지 않은 알림이 없으면 markAllAsRead를 건너뛴다', async () => {
    const store = useNotificationsStore();
    notificationsData.value = notificationsData.value.map((item) => ({ ...item, read: true }));

    await store.markAllAsRead();

    expect(markAllMutateAsyncMock).not.toHaveBeenCalled();
  });

  it('알림 추가와 전체 삭제를 mutation으로 위임한다', async () => {
    const store = useNotificationsStore();

    await store.add({
      title: '생성 알림',
      description: '설명',
      type: 'success',
      link: '/projects/1',
    });
    await store.clearAll();

    expect(createMutateAsyncMock).toHaveBeenCalledWith({
      title: '생성 알림',
      description: '설명',
      type: 'success',
      link: '/projects/1',
    });
    expect(clearMutateAsyncMock).toHaveBeenCalled();
  });
});
