<template>
  <Sidebar v-bind="props">
    <SidebarHeader>
      <SidebarMenu>
        <SidebarMenuItem>
          <SidebarMenuButton
            size="lg"
            variant="default"
            class="bg-transparent shadow-none ring-0 hover:bg-sidebar-accent/80 group-data-[collapsible=icon]:!size-11 group-data-[collapsible=icon]:!p-1"
            asChild
          >
            <RouterLink
              to="/"
              class="flex items-center gap-3 group-data-[collapsible=icon]:h-full group-data-[collapsible=icon]:w-full group-data-[collapsible=icon]:justify-center group-data-[collapsible=icon]:gap-0"
            >
              <div
                class="flex aspect-square size-8 shrink-0 items-center justify-center rounded-xl bg-primary text-sidebar-primary-foreground shadow-xs ring-1 ring-primary/10 group-data-[collapsible=icon]:size-8 group-data-[collapsible=icon]:rounded-xl group-data-[collapsible=icon]:shadow-xs group-data-[collapsible=icon]:ring-1 group-data-[collapsible=icon]:ring-primary/10"
              >
                <img
                  src="@/assets/main_logo.png"
                  alt="ABACUS Logo"
                  class="size-8 object-contain group-data-[collapsible=icon]:size-8"
                />
              </div>
              <div
                class="grid flex-1 text-left text-sm leading-tight transition-all duration-300 ease-in-out group-data-[collapsible=icon]:w-0 group-data-[collapsible=icon]:overflow-hidden group-data-[collapsible=icon]:opacity-0"
              >
                <span class="truncate font-semibold tracking-tight">ABACUS</span>
                <span class="truncate text-xs text-muted-foreground">통합 운영 대시보드</span>
              </div>
            </RouterLink>
          </SidebarMenuButton>
        </SidebarMenuItem>
      </SidebarMenu>
    </SidebarHeader>

    <SidebarContent class="gap-4">
      <NavMain :items="mainNavItems" label="업무" />
      <NavSecondary v-if="toolNavItems.length" :items="toolNavItems" label="도구" />
      <NavSecondary v-if="permissionNavItems.length" :items="permissionNavItems" label="권한" />
    </SidebarContent>

    <SidebarFooter>
      <NavUser :user="user" :onOpenProfileDialog="openProfileDialog" />
    </SidebarFooter>

    <SidebarRail />
  </Sidebar>
</template>

<script setup lang="ts">
import type { SidebarProps } from '@/components/ui/sidebar';
import { Bot, Briefcase, LayoutDashboard, Handshake, Network, ShieldCheck, UserCircle } from 'lucide-vue-next';
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { RouterLink } from 'vue-router';
import NavMain from './NavMain.vue';
import NavSecondary from './NavSecondary.vue';
import NavUser from './NavUser.vue';
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarRail,
  SidebarSeparator,
} from '@/components/ui/sidebar';
import { hasStoredPermission } from '@/features/auth/session';
import { canReadEmployeeDetail } from '@/features/employee/permissions';

const props = withDefaults(
  defineProps<
    SidebarProps & {
      onOpenProfileDialog?: () => void;
    }
  >(),
  {
    collapsible: 'icon',
    variant: 'inset',
    onOpenProfileDialog: () => {},
  },
);

const user = ref<{ name: string; email: string; avatar: string }>({
  name: 'User',
  email: 'user@example.com',
  avatar: '',
});
const canManagePermissionGroups = ref(false);
const canReadEmployees = ref(false);

function loadUserFromStorage() {
  try {
    const raw = localStorage.getItem('user');
    if (!raw) {
      canManagePermissionGroups.value = false;
      canReadEmployees.value = false;
      return;
    }

    const parsed = JSON.parse(raw);
    const name =
      parsed.name ||
      parsed.username ||
      [parsed.firstName, parsed.lastName].filter(Boolean).join(' ') ||
      'User';
    const email = parsed.email || parsed.username || parsed.userId || 'user@example.com';
    const avatar = parsed.avatarUrl || parsed.avatar || '';

    user.value = { name, email, avatar };
    canManagePermissionGroups.value = hasStoredPermission('permission.group.manage');
    canReadEmployees.value = canReadEmployeeDetail();
  } catch (error) {
    console.warn('Failed to parse user from localStorage', error);
    canManagePermissionGroups.value = false;
    canReadEmployees.value = false;
  }
}

function onStorage(event: StorageEvent) {
  if (event.key === 'user') {
    loadUserFromStorage();
  }
}

onMounted(() => {
  loadUserFromStorage();
  window.addEventListener('storage', onStorage);
});

onBeforeUnmount(() => {
  window.removeEventListener('storage', onStorage);
});

const mainNavItems = computed(() =>
  [
    {
      title: '대시보드',
      url: '/',
      icon: LayoutDashboard,
    },
    {
      title: '부서',
      url: '/departments',
      icon: Network,
    },
    {
      title: '직원',
      url: '/employees',
      icon: UserCircle,
    },
    {
      title: '프로젝트',
      url: '/projects',
      icon: Briefcase,
    },
    {
      title: '협력사',
      url: '/parties',
      icon: Handshake,
    },
  ].filter((item) => item.url !== '/employees' || canReadEmployees.value),
);

const toolNavItems = computed(() => [
  {
    title: 'AI Assistant',
    url: '/assistant',
    icon: Bot,
  },
]);

const permissionNavItems = computed(() =>
  [
    {
      title: '권한 그룹 관리',
      url: '/system/permission-groups',
      icon: ShieldCheck,
    },
  ].filter(() => canManagePermissionGroups.value),
);

function openProfileDialog() {
  props.onOpenProfileDialog();
}
</script>
