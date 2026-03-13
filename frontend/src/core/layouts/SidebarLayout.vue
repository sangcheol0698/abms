<template>
  <SidebarProvider>
    <div class="flex h-svh w-full overflow-hidden bg-sidebar text-foreground">
      <AppSidebar :onOpenProfileDialog="openProfileDialog" />
      <SidebarInset class="min-h-0 min-w-0 overflow-hidden">
        <header
          class="sticky top-0 z-10 flex h-16 items-center gap-3 border-b border-border/70 bg-background/80 px-3 md:px-5 lg:px-7 backdrop-blur-xl shadow-[0_18px_40px_-32px_color-mix(in_oklch,var(--foreground),transparent_55%)]"
        >
          <div class="flex flex-1 items-center gap-3">
            <SidebarTrigger class="-ml-1" />
            <Separator orientation="vertical" class="hidden h-6 md:block" />
            <div class="flex min-w-0 flex-col gap-1">
              <Breadcrumb v-if="breadcrumbs.length" class="hidden md:block">
                <BreadcrumbList class="text-sm">
                  <template v-for="(crumb, index) in breadcrumbs" :key="`${crumb.title}-${index}`">
                    <BreadcrumbItem>
                      <template v-if="crumb.to && !crumb.disabled">
                        <BreadcrumbLink
                          as-child
                          class="font-medium text-muted-foreground hover:text-foreground"
                        >
                          <RouterLink :to="crumb.to">{{ crumb.title }}</RouterLink>
                        </BreadcrumbLink>
                      </template>
                      <template v-else>
                        <BreadcrumbPage class="text-sm font-semibold">{{
                          crumb.title
                        }}</BreadcrumbPage>
                      </template>
                    </BreadcrumbItem>
                    <BreadcrumbSeparator
                      v-if="index < breadcrumbs.length - 1"
                      class="text-muted-foreground/70"
                    />
                  </template>
                </BreadcrumbList>
              </Breadcrumb>
              <slot name="title" />
            </div>
          </div>

          <div class="ml-auto flex items-center gap-2">
            <Button
              variant="field"
              size="sm"
              class="relative hidden h-10 w-56 justify-start rounded-xl text-sm font-normal text-foreground shadow-none sm:flex"
              @click="openCommandPalette"
            >
              <Search class="mr-2 h-4 w-4" />
              <span class="text-muted-foreground">검색...</span>
              <KbdGroup class="absolute right-2 items-center sm:flex">
                <Kbd>⌘</Kbd>
                <Kbd>K</Kbd>
              </KbdGroup>
            </Button>
            <div
              class="flex items-center gap-1 rounded-xl border border-border/60 bg-card/80 p-1 shadow-xs backdrop-blur"
            >
              <Button
                variant="ghost"
                size="icon"
                class="relative h-9 w-9 rounded-lg text-muted-foreground hover:bg-accent/70 hover:text-foreground"
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
              <ThemeToggle class="h-9 w-9" />
            </div>
          </div>
        </header>

        <main
          :class="['flex min-h-0 flex-1 flex-col overflow-y-auto overflow-x-hidden', paddingClass]"
        >
          <slot />
        </main>
      </SidebarInset>
      <AssistantDock />

      <ProfileDialog
        :open="isProfileDialogOpen"
        @update:open="isProfileDialogOpen = $event"
        :user="user"
        :open-self-profile-editor="openSelfProfileEditor"
      />
      <CommandPalette :open="isCommandPaletteOpen" @update:open="isCommandPaletteOpen = $event" />
      <NotificationsSheet
        :open="isNotificationsOpen"
        @update:open="(value) => (isNotificationsOpen = value)"
      />
    </div>
    <SidebarLayoutController />
  </SidebarProvider>
</template>

<script setup lang="ts">
import { computed, onMounted, onBeforeUnmount, ref, watch, defineComponent } from 'vue';
import { useRoute } from 'vue-router';
import { RouterLink } from 'vue-router';
import { Bell, Search } from 'lucide-vue-next';
import { SidebarInset, SidebarProvider, SidebarTrigger, useSidebar } from '@/components/ui/sidebar';
import { Separator } from '@/components/ui/separator';
import { Button } from '@/components/ui/button';
import AppSidebar from '@/components/sidebar/AppSidebar.vue';
import ThemeToggle from '@/core/theme/ThemeToggle.vue';
import CommandPalette from '@/components/business/CommandPalette.vue';
import AssistantDock from '@/features/chat/components/AssistantDock.vue';
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from '@/components/ui/breadcrumb';
import NotificationsSheet from '@/components/business/NotificationsSheet.vue';
import ProfileDialog from '@/components/business/ProfileDialog.vue';
import { useNotificationsStore } from '@/core/stores/notifications.store';
import { storeToRefs } from 'pinia';
import { Kbd, KbdGroup } from '@/components/ui/kbd';
import { addOpenProfileDialogListener } from '@/features/auth/profileDialogEvents';

const route = useRoute();

/**
 * SidebarLayoutController
 * SidebarProvider 내부에서 useSidebar()를 호출하기 위한 내부 컴포넌트
 */
const SidebarLayoutController = defineComponent({
  setup() {
    const route = useRoute();
    const sidebar = useSidebar();

    watch(
      () => route.path,
      () => {
        if (sidebar.isMobile.value) {
          sidebar.setOpenMobile(false);
        }
      },
    );

    return () => null;
  },
});

interface BreadcrumbEntry {
  title: string;
  to?: string;
  disabled?: boolean;
}

const breadcrumbs = computed<BreadcrumbEntry[]>(() => {
  const metaCrumbs = route.meta?.breadcrumbs as
    | Array<{ title: string; to?: string | ((payload: any) => string); disabled?: boolean }>
    | undefined;

  if (Array.isArray(metaCrumbs) && metaCrumbs.length > 0) {
    return metaCrumbs.map((crumb) => {
      let to: string | undefined;
      if (typeof crumb.to === 'function') {
        try {
          to = crumb.to(route);
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

const paddingClass = computed(() => {
  const mode = route.meta?.padding as string | undefined;
  if (mode === 'compact') {
    return 'p-4 sm:p-6 md:px-8 lg:px-12';
  }
  if (mode === 'comfortable') {
    return 'p-6 lg:p-12';
  }
  if (mode === 'flush') {
    return 'p-0';
  }
  // Default padding with horizontal container-like margins
  return 'p-6 md:px-8 lg:px-12';
});

const isProfileDialogOpen = ref(false);
const isCommandPaletteOpen = ref(false);
const isNotificationsOpen = ref(false);
const openSelfProfileEditor = ref(false);
let removeProfileDialogListener = () => {};
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
    const raw = typeof window !== 'undefined' ? localStorage.getItem('user') : null;
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
  void notificationsStore.fetchAll();
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
  removeProfileDialogListener = addOpenProfileDialogListener((detail) => {
    openSelfProfileEditor.value = Boolean(detail?.openSelfProfileEditor);
    openProfileDialog();
  });
  void notificationsStore.fetchAll();
});

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeydown);
  window.removeEventListener('storage', handleStorage);
  removeProfileDialogListener();
});
</script>
