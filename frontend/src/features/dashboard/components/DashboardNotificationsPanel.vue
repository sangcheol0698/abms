<template>
  <Card>
    <CardHeader class="flex flex-row items-start justify-between gap-3 space-y-0">
      <div class="space-y-1">
        <CardTitle>최근 알림</CardTitle>
        <CardDescription>지금 확인이 필요한 운영 이벤트입니다.</CardDescription>
      </div>
      <Badge v-if="unreadCount > 0" variant="secondary">{{ unreadCount }}</Badge>
    </CardHeader>
    <CardContent class="space-y-3">
      <div
        v-if="isLoading && notifications.length === 0"
        class="flex h-[220px] items-center justify-center text-sm text-muted-foreground"
      >
        알림을 불러오는 중입니다...
      </div>
      <template v-else-if="notifications.length > 0">
        <div
          v-for="notification in notifications"
          :key="notification.id"
          class="flex items-start justify-between rounded-md border border-border/70 p-3 text-sm"
        >
          <div class="space-y-1">
            <p class="font-medium text-foreground">{{ notification.title }}</p>
            <p class="text-xs text-muted-foreground">
              {{ notification.description ?? '상세 내용이 없습니다.' }}
            </p>
          </div>
          <Badge variant="outline" class="shrink-0 text-[11px] uppercase tracking-tight">
            {{ formatRelativeTimestamp(notification.createdAt) }}
          </Badge>
        </div>
      </template>
      <p v-else class="flex h-[220px] items-center justify-center text-sm text-muted-foreground">
        최근 알림이 없습니다.
      </p>
    </CardContent>
  </Card>
</template>

<script setup lang="ts">
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import type { NotificationItem } from '@/features/notification/models/notification';
import { formatRelativeTimestamp } from '@/features/dashboard/utils/format';

withDefaults(
  defineProps<{
    notifications?: NotificationItem[];
    unreadCount?: number;
    isLoading?: boolean;
  }>(),
  {
    notifications: () => [],
    unreadCount: 0,
    isLoading: false,
  },
);
</script>
