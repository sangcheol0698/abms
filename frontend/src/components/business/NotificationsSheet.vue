<template>
  <Sheet :open="props.open" @update:open="emit('update:open', $event)">
    <SheetContent side="right" class="flex w-full max-w-[420px] flex-col gap-0 p-0">
      <header class="flex items-center justify-between border-b border-border/60 pl-4 pr-14 py-3">
        <div class="flex items-center gap-2">
          <Bell class="h-4 w-4" />
          <h2 class="text-sm font-semibold">알림</h2>
          <Badge v-if="unreadCount > 0" variant="destructive" class="h-5 min-w-5 text-[11px]">
            {{ unreadCount }}
          </Badge>
        </div>
        <div class="flex items-center gap-2">
          <Button
            size="sm"
            variant="ghost"
            class="h-8 px-2"
            :disabled="unreadCount === 0"
            @click="markAllAsRead"
          >
            모두 읽음 처리
          </Button>
          <Button
            size="icon"
            variant="ghost"
            class="h-8 w-8"
            :disabled="items.length === 0"
            @click="clearAll"
          >
            <Trash2 class="h-4 w-4" />
          </Button>
        </div>
      </header>

      <section class="flex-1 space-y-3 px-4 py-3">
        <div class="flex items-center gap-2">
          <Button
            size="sm"
            variant="outline"
            :class="tab === 'all' ? 'bg-accent text-accent-foreground shadow-sm' : ''"
            @click="tab = 'all'"
          >
            전체
          </Button>
          <Button
            size="sm"
            variant="outline"
            :class="tab === 'unread' ? 'bg-accent text-accent-foreground shadow-sm' : ''"
            @click="tab = 'unread'"
          >
            안읽음
          </Button>
        </div>

        <p
          v-if="filtered.length === 0"
          class="rounded-md border border-dashed border-border/60 bg-muted/30 py-10 text-center text-sm text-muted-foreground"
        >
          표시할 알림이 없습니다.
        </p>

        <ul v-else class="space-y-2 overflow-y-auto pr-1" style="max-height: calc(100vh - 210px)">
          <li v-for="notification in filtered" :key="notification.id">
            <button
              variant="ghost"
              class="flex w-full justify-start gap-3 rounded-md px-3 py-2 text-left hover:bg-accent/70"
              @click="openNotification(notification)"
            >
              <span :class="indicatorClass(notification.type)" />
              <div class="min-w-0 flex-1">
                <div class="flex items-center gap-2">
                  <p class="truncate text-sm font-medium text-foreground">
                    {{ notification.title }}
                  </p>
                  <span v-if="!notification.read" class="h-2 w-2 rounded-full bg-primary" />
                </div>
                <p
                  v-if="notification.description"
                  class="mt-1 line-clamp-2 text-xs text-muted-foreground"
                >
                  {{ notification.description }}
                </p>
                <p class="mt-2 text-[11px] uppercase tracking-wide text-muted-foreground">
                  {{ formatRelative(notification.createdAt) }}
                </p>
              </div>
            </button>
          </li>
        </ul>

        <div class="flex justify-end">
          <Button size="sm" variant="ghost" @click="emit('update:open', false)">닫기</Button>
        </div>
      </section>
    </SheetContent>
  </Sheet>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import { Bell, Trash2 } from 'lucide-vue-next';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Sheet, SheetContent } from '@/components/ui/sheet';
import { useNotificationsStore, type NotificationItem } from '@/core/stores/notifications.store';

const props = withDefaults(
  defineProps<{
    open?: boolean;
  }>(),
  {
    open: false,
  },
);

const emit = defineEmits<{
  (event: 'update:open', value: boolean): void;
}>();

const router = useRouter();
const store = useNotificationsStore();
const { unreadCount, sorted: items } = storeToRefs(store);
const tab = ref<'all' | 'unread'>('all');

const filtered = computed(() =>
  tab.value === 'all' ? items.value : items.value.filter((notification) => !notification.read),
);

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      tab.value = 'all';
    }
  },
);

function indicatorClass(type: NotificationItem['type']) {
  switch (type) {
    case 'success':
      return 'mt-1 h-2.5 w-2.5 flex-none rounded-full bg-emerald-500';
    case 'warning':
      return 'mt-1 h-2.5 w-2.5 flex-none rounded-full bg-amber-400';
    case 'error':
      return 'mt-1 h-2.5 w-2.5 flex-none rounded-full bg-rose-500';
    default:
      return 'mt-1 h-2.5 w-2.5 flex-none rounded-full bg-sky-500';
  }
}

function formatRelative(isoString: string) {
  const created = new Date(isoString);
  const now = new Date();
  const diffMs = now.getTime() - created.getTime();

  if (diffMs < 0) {
    return '방금 전';
  }

  const diffMinutes = Math.floor(diffMs / 60000);
  if (diffMinutes < 1) return '방금 전';
  if (diffMinutes < 60) return `${diffMinutes}분 전`;
  const diffHours = Math.floor(diffMinutes / 60);
  if (diffHours < 24) return `${diffHours}시간 전`;
  const diffDays = Math.floor(diffHours / 24);
  if (diffDays < 7) return `${diffDays}일 전`;
  return created.toLocaleDateString();
}

function markAllAsRead() {
  store.markAllAsRead();
}

function clearAll() {
  store.clearAll();
  emit('update:open', false);
}

async function openNotification(notification: NotificationItem) {
  store.markAsRead(notification.id);
  emit('update:open', false);
  if (notification.link) {
    try {
      await router.push(notification.link);
    } catch (error) {
      console.warn('Failed to navigate to notification link', error);
    }
  }
}
</script>
