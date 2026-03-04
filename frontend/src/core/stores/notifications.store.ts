import { computed } from 'vue';
import { defineStore } from 'pinia';
import {
  useClearNotificationsMutation,
  useCreateNotificationMutation,
  useMarkAllNotificationsAsReadMutation,
  useMarkNotificationAsReadMutation,
  useNotificationsQuery,
} from '@/features/notification/queries/useNotificationQueries';
import type { NotificationItem, NotificationType } from '@/features/notification/models/notification';

export type { NotificationItem, NotificationType };

export const useNotificationsStore = defineStore('notifications', () => {
  const notificationsQuery = useNotificationsQuery();
  const markAsReadMutation = useMarkNotificationAsReadMutation();
  const markAllAsReadMutation = useMarkAllNotificationsAsReadMutation();
  const createMutation = useCreateNotificationMutation();
  const clearMutation = useClearNotificationsMutation();

  const items = computed<NotificationItem[]>(() => notificationsQuery.data.value ?? []);
  const isLoading = computed(
    () => notificationsQuery.isFetching.value || markAsReadMutation.isPending.value,
  );

  const unreadCount = computed(() => items.value.filter((item) => !item.read).length);
  const sorted = computed(() =>
    [...items.value].sort(
      (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime(),
    ),
  );

  async function fetchAll() {
    await notificationsQuery.refetch();
  }

  async function markAsRead(id: number) {
    const target = items.value.find((item) => item.id === id);
    if (!target || target.read) {
      return;
    }

    await markAsReadMutation.mutateAsync(id);
  }

  async function markAllAsRead() {
    if (unreadCount.value === 0) {
      return;
    }

    await markAllAsReadMutation.mutateAsync();
  }

  async function add(
    notification: Omit<NotificationItem, 'id' | 'createdAt' | 'read'> & {
      createdAt?: string;
      read?: boolean;
    },
  ) {
    await createMutation.mutateAsync({
      title: notification.title,
      description: notification.description,
      type: notification.type,
      link: notification.link,
    });
  }

  async function clearAll() {
    if (items.value.length === 0) {
      return;
    }

    await clearMutation.mutateAsync();
  }

  return {
    items,
    isLoading,
    unreadCount,
    sorted,
    fetchAll,
    markAsRead,
    markAllAsRead,
    add,
    clearAll,
  };
});
