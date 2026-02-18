import { inject, singleton } from 'tsyringe';
import HttpRepository from '@/core/http/HttpRepository';
import {
  normalizeNotificationType,
  type NotificationItem,
  type NotificationType,
} from '@/features/notification/models/notification';

interface NotificationResponse {
  id: number;
  title: string;
  description?: string | null;
  type: string;
  createdAt: string;
  read: boolean;
  link?: string | null;
}

interface NotificationCreateData {
  title: string;
  description?: string;
  type?: NotificationType;
  link?: string;
}

@singleton()
export default class NotificationRepository {
  constructor(@inject(HttpRepository) private readonly httpRepository: HttpRepository) {}

  async fetchAll(): Promise<NotificationItem[]> {
    const response = await this.httpRepository.get({
      path: '/api/notifications',
    });

    if (!Array.isArray(response)) {
      return [];
    }

    return response.map((notification) => this.mapNotification(notification as NotificationResponse));
  }

  async create(data: NotificationCreateData): Promise<NotificationItem> {
    const response = (await this.httpRepository.post({
      path: '/api/notifications',
      data: {
        title: data.title,
        description: data.description,
        type: this.toApiType(data.type),
        link: data.link,
      },
    })) as NotificationResponse;

    return this.mapNotification(response);
  }

  async markAsRead(notificationId: number): Promise<void> {
    await this.httpRepository.patch({
      path: `/api/notifications/${notificationId}/read`,
    });
  }

  async markAllAsRead(): Promise<void> {
    await this.httpRepository.patch({
      path: '/api/notifications/read-all',
    });
  }

  async clearAll(): Promise<void> {
    await this.httpRepository.delete({
      path: '/api/notifications',
    });
  }

  private mapNotification(notification: NotificationResponse): NotificationItem {
    return {
      id: notification.id,
      title: notification.title,
      description: notification.description ?? undefined,
      type: normalizeNotificationType(notification.type),
      createdAt: notification.createdAt,
      read: notification.read,
      link: notification.link ?? undefined,
    };
  }

  private toApiType(type: NotificationType | undefined): string {
    switch (type) {
      case 'success':
        return 'SUCCESS';
      case 'warning':
        return 'WARNING';
      case 'error':
        return 'ERROR';
      default:
        return 'INFO';
    }
  }
}

