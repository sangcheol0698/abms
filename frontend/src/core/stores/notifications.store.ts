import { defineStore } from 'pinia';

export type NotificationType = 'info' | 'success' | 'warning' | 'error';

export interface NotificationItem {
  id: string;
  title: string;
  description?: string;
  type: NotificationType;
  createdAt: string;
  read: boolean;
  link?: string;
}

function toIsoMinutesAgo(minutes: number): string {
  const date = new Date(Date.now() - minutes * 60_000);
  return date.toISOString();
}

export const useNotificationsStore = defineStore('notifications', {
  state: () => ({
    items: [
      {
        id: 'n-organization',
        title: '조직 구조 변경',
        description: '조직도에서 경영기획실이 신설되었습니다.',
        type: 'info' as NotificationType,
        createdAt: toIsoMinutesAgo(3),
        read: false,
        link: '/',
      },
      {
        id: 'n-employee',
        title: '직원 정보 갱신',
        description: '인사팀이 구성원 정보를 최신화했습니다.',
        type: 'success' as NotificationType,
        createdAt: toIsoMinutesAgo(18),
        read: false,
        link: '/employees',
      },
      {
        id: 'n-policy',
        title: '근태 정책 안내',
        description: '다음 주부터 신규 근태 정책이 적용됩니다.',
        type: 'warning' as NotificationType,
        createdAt: toIsoMinutesAgo(180),
        read: true,
      },
    ] as NotificationItem[],
  }),
  getters: {
    unreadCount: (state) => state.items.filter((item) => !item.read).length,
    sorted: (state) =>
      [...state.items].sort(
        (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime(),
      ),
  },
  actions: {
    markAsRead(id: string) {
      const target = this.items.find((item) => item.id === id);
      if (target) {
        target.read = true;
      }
    },
    markAllAsRead() {
      this.items = this.items.map((item) => ({ ...item, read: true }));
    },
    add(
      notification: Omit<NotificationItem, 'id' | 'createdAt' | 'read'> & {
        id?: string;
        createdAt?: string;
        read?: boolean;
      },
    ) {
      const id = notification.id ?? `n-${Math.random().toString(36).slice(2, 9)}`;
      const createdAt = notification.createdAt ?? new Date().toISOString();
      const read = notification.read ?? false;

      this.items = [
        {
          ...notification,
          id,
          createdAt,
          read,
        },
        ...this.items,
      ];
    },
    clearAll() {
      this.items = [];
    },
  },
});
