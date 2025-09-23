<template>
  <div class="flex min-h-svh w-full bg-background text-foreground">
    <AppSidebar :onOpenProfileDialog="openProfileDialog" />
    <SidebarInset>
      <header
        class="sticky top-0 z-10 flex h-16 items-center gap-3 border-b border-border bg-background/95 px-4 backdrop-blur"
      >
        <div class="flex flex-1 items-center gap-3">
          <SidebarTrigger class="-ml-1" />
          <Separator orientation="vertical" class="hidden h-6 md:block" />
          <div class="flex min-w-0 flex-col gap-1">
            <Breadcrumb v-if="breadcrumbs.length" class="hidden md:block">
              <BreadcrumbList>
                <template v-for="(crumb, index) in breadcrumbs" :key="`${crumb.title}-${index}`">
                  <BreadcrumbItem>
                    <template v-if="crumb.to && !crumb.disabled">
                      <BreadcrumbLink as-child>
                        <RouterLink :to="crumb.to">{{ crumb.title }}</RouterLink>
                      </BreadcrumbLink>
                    </template>
                    <template v-else>
                      <BreadcrumbPage>{{ crumb.title }}</BreadcrumbPage>
                    </template>
                  </BreadcrumbItem>
                  <BreadcrumbSeparator v-if="index < breadcrumbs.length - 1" />
                </template>
              </BreadcrumbList>
            </Breadcrumb>
            <slot name="title" />
          </div>
        </div>

        <div class="ml-auto flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            class="relative hidden h-9 w-48 justify-start text-xs font-normal text-muted-foreground shadow-none sm:flex"
            @click="openCommandPalette"
          >
            <Search class="mr-2 h-4 w-4" />
            <span class="hidden lg:inline">검색...</span>
            <span class="lg:hidden">검색</span>
            <kbd
              class="pointer-events-none absolute right-2 top-1/2 hidden -translate-y-1/2 select-none items-center gap-1 rounded border bg-muted px-1.5 font-mono text-[10px] font-medium opacity-100 sm:flex"
            >
              <span class="text-xs">⌘</span>K
            </kbd>
          </Button>
          <Button
            variant="outline"
            size="icon"
            class="relative h-9 w-9"
            @click="toggleNotifications"
          >
            <Bell class="h-4 w-4" />
            <span class="sr-only">알림 보기</span>
            <span
              v-if="unreadCount > 0"
              class="absolute -right-1 -top-1 flex h-4 w-4 items-center justify-center rounded-full bg-destructive text-[10px] font-semibold text-destructive-foreground"
            >
              {{ unreadCount > 9 ? '9+' : unreadCount }}
            </span>
          </Button>
          <ThemeToggle />
        </div>
      </header>

      <main class="flex-1 overflow-y-auto p-6">
        <slot />
      </main>
    </SidebarInset>

    <ProfileDialog
      :open="isProfileDialogOpen"
      @update:open="isProfileDialogOpen = $event"
      :user="user"
    />
    <CommandPalette :open="isCommandPaletteOpen" @update:open="isCommandPaletteOpen = $event" />
    <NotificationsSheet
      :open="isNotificationsOpen"
      @update:open="(value) => (isNotificationsOpen = value)"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onBeforeUnmount, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { RouterLink } from 'vue-router';
import { Bell, Search } from 'lucide-vue-next';
import { SidebarInset, SidebarTrigger } from '@/components/ui/sidebar';
import { Separator } from '@/components/ui/separator';
import { Button } from '@/components/ui/button';
import AppSidebar from '@/components/sidebar/AppSidebar.vue';
import ThemeToggle from '@/core/theme/ThemeToggle.vue';
import CommandPalette from '@/components/business/CommandPalette.vue';
import { Breadcrumb, BreadcrumbItem, BreadcrumbLink, BreadcrumbList, BreadcrumbPage, BreadcrumbSeparator } from '@/components/ui/breadcrumb';
import NotificationsSheet from '@/components/business/NotificationsSheet.vue';
import ProfileDialog from '@/components/business/ProfileDialog.vue';
import { useNotificationsStore } from '@/core/stores/notifications.store';
import { storeToRefs } from 'pinia';

const route = useRoute();

interface BreadcrumbEntry {
  title: string;
  to?: string;
  disabled?: boolean;
}

const breadcrumbs = computed<BreadcrumbEntry[]>(() => {
  const metaCrumbs = route.meta?.breadcrumbs as Array<
    { title: string; to?: string | ((payload: any) => string); disabled?: boolean }
  > | undefined;

  if (Array.isArray(metaCrumbs) && metaCrumbs.length > 0) {
    return metaCrumbs.map((crumb) => {
      let to: string | undefined;
      if (typeof crumb.to === 'function') {
        try {
          const resolved = crumb.to(route);
          if (typeof resolved === 'string') {
            to = resolved;
          }
        } catch (error) {
          console.warn('Failed to resolve breadcrumb path', error);
        }
      } else if (typeof crumb.to === 'string') {
        to = crumb.to;
      }
      return {
        title: crumb.title,
        to,
        disabled: crumb.disabled ?? !to,
      };
    });
  }

  if (typeof route.meta?.title === 'string') {
    return [
      {
        title: route.meta.title as string,
        disabled: true,
      },
    ];
  }

  return [];
});

const isProfileDialogOpen = ref(false);
const isCommandPaletteOpen = ref(false);
const isNotificationsOpen = ref(false);
const notificationsStore = useNotificationsStore();
const { unreadCount } = storeToRefs(notificationsStore);

const user = ref({
  name: 'User',
  email: 'user@example.com',
  avatar: '',
});

function parseUser(value: unknown) {
  if (!value || typeof value !== 'object') {
    return;
  }

  const candidate = value as Record<string, unknown>;
  const name =
    (candidate.name as string | undefined) ||
    (candidate.username as string | undefined) ||
    [candidate.firstName, candidate.lastName]
      .filter((part) => typeof part === 'string' && part !== '')
      .join(' ') ||
    undefined;

  const email =
    (candidate.email as string | undefined) ||
    (candidate.username as string | undefined) ||
    (candidate.userId as string | undefined);

  const avatar =
    (candidate.avatarUrl as string | undefined) || (candidate.avatar as string | undefined);

  user.value = {
    name: name ?? 'User',
    email: email ?? 'user@example.com',
    avatar: avatar ?? '',
  };
}

function loadUserFromStorage() {
  try {
    const raw = typeof window !== 'undefined' ? window.localStorage.getItem('user') : null;
    if (!raw) {
      return;
    }

    parseUser(JSON.parse(raw));
  } catch (error) {
    console.warn('Failed to read user from storage', error);
  }
}

function handleStorage(event: StorageEvent) {
  if (event.key === 'user') {
    loadUserFromStorage();
  }
}

function openProfileDialog() {
  isProfileDialogOpen.value = true;
}

function openCommandPalette() {
  isCommandPaletteOpen.value = true;
}

function toggleNotifications() {
  isNotificationsOpen.value = true;
}

function handleKeydown(event: KeyboardEvent) {
  if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === 'k') {
    event.preventDefault();
    openCommandPalette();
  }
}

onMounted(() => {
  loadUserFromStorage();
  document.addEventListener('keydown', handleKeydown);
  window.addEventListener('storage', handleStorage);
});

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeydown);
  window.removeEventListener('storage', handleStorage);
});

watch(isNotificationsOpen, (next) => {
  if (next) {
    notificationsStore.markAllAsRead();
  }
});
</script>
