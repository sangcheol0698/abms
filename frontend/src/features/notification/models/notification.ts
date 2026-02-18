export type NotificationType = 'info' | 'success' | 'warning' | 'error';

export interface NotificationItem {
  id: number;
  title: string;
  description?: string;
  type: NotificationType;
  createdAt: string;
  read: boolean;
  link?: string;
}

export function normalizeNotificationType(rawType: string | null | undefined): NotificationType {
  switch (rawType?.toUpperCase()) {
    case 'SUCCESS':
      return 'success';
    case 'WARNING':
      return 'warning';
    case 'ERROR':
      return 'error';
    default:
      return 'info';
  }
}

