import { useMutation, useQuery } from '@tanstack/vue-query';
import { appContainer } from '@/core/di/container';
import { notificationKeys, queryClient } from '@/core/query';
import NotificationRepository from '@/features/notification/repository/NotificationRepository';
import type { NotificationType } from '@/features/notification/models/notification';

async function invalidateNotifications() {
  await queryClient.invalidateQueries({ queryKey: notificationKeys.all });
}

export function useNotificationsQuery() {
  const repository = appContainer.resolve(NotificationRepository);

  return useQuery({
    queryKey: notificationKeys.list(),
    queryFn: () => repository.fetchAll(),
  });
}

export function useMarkNotificationAsReadMutation() {
  const repository = appContainer.resolve(NotificationRepository);

  return useMutation({
    mutationFn: (notificationId: number) => repository.markAsRead(notificationId),
    onSuccess: invalidateNotifications,
  });
}

export function useMarkAllNotificationsAsReadMutation() {
  const repository = appContainer.resolve(NotificationRepository);

  return useMutation({
    mutationFn: () => repository.markAllAsRead(),
    onSuccess: invalidateNotifications,
  });
}

export function useCreateNotificationMutation() {
  const repository = appContainer.resolve(NotificationRepository);

  return useMutation({
    mutationFn: (data: {
      title: string;
      description?: string;
      type?: NotificationType;
      link?: string;
    }) => repository.create(data),
    onSuccess: invalidateNotifications,
  });
}

export function useClearNotificationsMutation() {
  const repository = appContainer.resolve(NotificationRepository);

  return useMutation({
    mutationFn: () => repository.clearAll(),
    onSuccess: invalidateNotifications,
  });
}
