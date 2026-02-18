import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import { appContainer } from '@/core/di/container';
import NotificationRepository from '@/features/notification/repository/NotificationRepository';
import type { NotificationItem, NotificationType } from '@/features/notification/models/notification';

export type { NotificationItem, NotificationType };

export const useNotificationsStore = defineStore('notifications', () => {
  const repository = appContainer.resolve(NotificationRepository);

  const items = ref<NotificationItem[]>([]);
  const isLoading = ref(false);
  const isLoaded = ref(false);

  const unreadCount = computed(() => items.value.filter((item) => !item.read).length);
  const sorted = computed(() =>
    [...items.value].sort(
      (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime(),
    ),
  );

  async function fetchAll(options: { force?: boolean } = {}) {
    if (isLoading.value) {
      return;
    }
    if (isLoaded.value && !options.force) {
      return;
    }

    isLoading.value = true;
    try {
      items.value = await repository.fetchAll();
      isLoaded.value = true;
    } catch (error) {
      console.error('Failed to fetch notifications:', error);
    } finally {
      isLoading.value = false;
    }
  }

  async function markAsRead(id: number) {
    const target = items.value.find((item) => item.id === id);
    if (!target || target.read) {
      return;
    }

    await repository.markAsRead(id);
    target.read = true;
  }

  async function markAllAsRead() {
    if (unreadCount.value === 0) {
      return;
    }

    await repository.markAllAsRead();
    items.value = items.value.map((item) => ({ ...item, read: true }));
  }

  async function add(
    notification: Omit<NotificationItem, 'id' | 'createdAt' | 'read'> & {
      createdAt?: string;
      read?: boolean;
    },
  ) {
    const created = await repository.create({
      title: notification.title,
      description: notification.description,
      type: notification.type,
      link: notification.link,
    });

    items.value = [created, ...items.value];
  }

  async function clearAll() {
    if (items.value.length === 0) {
      return;
    }

    await repository.clearAll();
    items.value = [];
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
