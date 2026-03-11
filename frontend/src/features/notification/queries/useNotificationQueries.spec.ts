import { beforeEach, describe, expect, it, vi } from 'vitest';
import { notificationKeys, queryClient } from '@/core/query';
import {
  useClearNotificationsMutation,
  useCreateNotificationMutation,
  useMarkAllNotificationsAsReadMutation,
  useMarkNotificationAsReadMutation,
  useNotificationsQuery,
} from '@/features/notification/queries/useNotificationQueries';

const vueQueryMocks = vi.hoisted(() => ({
  useQueryMock: vi.fn((options) => options),
  useMutationMock: vi.fn((options) => options),
}));

const notificationRepositoryMock = {
  fetchAll: vi.fn(),
  markAsRead: vi.fn(),
  markAllAsRead: vi.fn(),
  create: vi.fn(),
  clearAll: vi.fn(),
};

vi.mock('@tanstack/vue-query', async () => {
  const actual = await vi.importActual<typeof import('@tanstack/vue-query')>('@tanstack/vue-query');
  return {
    ...actual,
    useQuery: vueQueryMocks.useQueryMock,
    useMutation: vueQueryMocks.useMutationMock,
  };
});

vi.mock('@/core/di/container', () => ({
  appContainer: {
    resolve: () => notificationRepositoryMock,
  },
}));

describe('useNotificationQueries', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.spyOn(queryClient, 'invalidateQueries').mockResolvedValue(undefined as never);
  });

  it('목록 query를 저장소와 연결한다', async () => {
    notificationRepositoryMock.fetchAll.mockResolvedValueOnce('items');

    const query = useNotificationsQuery();

    expect(query.queryKey).toEqual(notificationKeys.list());
    await expect(query.queryFn()).resolves.toBe('items');
  });

  it('변경 mutation 성공 시 알림 query를 invalidate한다', async () => {
    const markAsReadMutation = useMarkNotificationAsReadMutation();
    const markAllMutation = useMarkAllNotificationsAsReadMutation();
    const createMutation = useCreateNotificationMutation();
    const clearMutation = useClearNotificationsMutation();

    await markAsReadMutation.mutationFn(1);
    await markAllMutation.mutationFn();
    await createMutation.mutationFn({ title: '알림' });
    await clearMutation.mutationFn();
    await markAsReadMutation.onSuccess?.();
    await markAllMutation.onSuccess?.();
    await createMutation.onSuccess?.();
    await clearMutation.onSuccess?.();

    expect(notificationRepositoryMock.markAsRead).toHaveBeenCalledWith(1);
    expect(notificationRepositoryMock.markAllAsRead).toHaveBeenCalled();
    expect(notificationRepositoryMock.create).toHaveBeenCalledWith({ title: '알림' });
    expect(notificationRepositoryMock.clearAll).toHaveBeenCalled();
    expect(queryClient.invalidateQueries).toHaveBeenCalledWith({
      queryKey: notificationKeys.all,
    });
  });
});
