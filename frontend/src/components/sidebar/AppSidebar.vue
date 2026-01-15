<template>
  <Sidebar v-bind="props">
    <SidebarHeader>
      <SidebarMenu>
        <SidebarMenuItem>
          <SidebarMenuButton size="lg" asChild>
            <router-link to="/" class="flex items-center gap-3">
              <div
                  class="flex aspect-square size-8 items-center justify-center rounded-lg bg-primary text-sidebar-primary-foreground transition-all duration-300"
              >
                <img src="@/assets/main_logo.png" alt="ABACUS Logo" class="size-8" />
              </div>
              <div
                  class="flex flex-col gap-0.5 leading-none transition-all duration-300 ease-in-out group-data-[collapsible=icon]:opacity-0 group-data-[collapsible=icon]:w-0 group-data-[collapsible=icon]:overflow-hidden"
              >
                <span class="font-semibold">ABACUS</span>
                <span class="text-xs text-muted-foreground"> Abacus Inc </span>
              </div>
            </router-link>
          </SidebarMenuButton>
        </SidebarMenuItem>
      </SidebarMenu>
    </SidebarHeader>

    <SidebarContent>
      <NavMain :items="data.navMain" label="업무" />
      <NavMain v-if="data.navSecondary.length" :items="data.navSecondary" label="기타" />
    </SidebarContent>

    <SidebarFooter>
      <NavUser :user="user" :onOpenProfileDialog="openProfileDialog" />
    </SidebarFooter>

    <SidebarRail />
  </Sidebar>
</template>

<script setup lang="ts">
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  type SidebarProps,
  SidebarRail
} from '@/components/ui/sidebar';
import { Bot, Briefcase, GalleryVerticalEnd, Network, PieChart, UserCircle } from 'lucide-vue-next';
import NavMain from './NavMain.vue';
import NavUser from './NavUser.vue';
import { onBeforeUnmount, onMounted, ref } from 'vue';

const props = withDefaults(
    defineProps<
        SidebarProps & {
      onOpenProfileDialog?: () => void;
    }
    >(),
    {
      collapsible: 'icon',
      onOpenProfileDialog: () => {},
    }
);

// 사용자 정보: localStorage에서 로드
const user = ref<{ name: string; email: string; avatar: string }>({
  name: 'User',
  email: 'user@example.com',
  avatar: '',
});

function loadUserFromStorage() {
  try {
    const raw = localStorage.getItem('user');
    if (!raw) return;
    const parsed = JSON.parse(raw);

    // 다양한 필드 네이밍을 고려한 안전 추출
    const name =
        parsed.name ||
        parsed.username ||
        [parsed.firstName, parsed.lastName].filter(Boolean).join(' ') ||
        'User';
    const email = parsed.email || parsed.username || parsed.userId || 'user@example.com';
    const avatar = parsed.avatarUrl || parsed.avatar || '';

    user.value = { name, email, avatar };
  } catch (e) {
    console.warn('Failed to parse user from localStorage', e);
  }
}

function onStorage(e: StorageEvent) {
  if (e.key === 'user') {
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

// 네비게이션 항목
const data = {
  teams: [
    {
      name: 'Abacus Inc',
      logo: GalleryVerticalEnd,
      plan: 'Enterprise',
    },
  ],
  navMain: [
    {
      title: '대시보드',
      url: '/',
      icon: PieChart,
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
      title: 'AI Assistant',
      url: '/assistant',
      icon: Bot
    }
  ],
  navSecondary: [],
};

// 프로필 다이얼로그 열기
function openProfileDialog() {
  props.onOpenProfileDialog();
}
</script>
