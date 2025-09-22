<template>
  <SidebarMenu>
    <SidebarMenuItem>
      <DropdownMenu>
        <DropdownMenuTrigger as-child>
          <SidebarMenuButton
            size="lg"
            class="data-[state=open]:bg-sidebar-accent data-[state=open]:text-sidebar-accent-foreground"
          >
            <Avatar class="h-8 w-8 rounded-lg">
              <AvatarImage :src="user.avatar" :alt="user.name" />
              <AvatarFallback class="rounded-lg">{{ user.name?.charAt(0) || 'U' }}</AvatarFallback>
            </Avatar>
            <div class="grid flex-1 text-left text-sm leading-tight">
              <span class="truncate font-semibold">{{ user.name }}</span>
              <span class="truncate text-xs">{{ user.email }}</span>
            </div>
            <ChevronsUpDown class="ml-auto size-4" />
          </SidebarMenuButton>
        </DropdownMenuTrigger>
        <DropdownMenuContent
          class="w-[--reka-popper-anchor-width] min-w-56 rounded-lg"
          side="bottom"
          align="end"
          :side-offset="4"
        >
          <DropdownMenuLabel class="p-0 font-normal">
            <div class="flex items-center gap-2 px-1 py-1.5 text-left text-sm">
              <Avatar class="h-8 w-8 rounded-lg">
                <AvatarImage :src="user.avatar" :alt="user.name" />
                <AvatarFallback class="rounded-lg">{{ user.name?.charAt(0) || 'U' }}</AvatarFallback>
              </Avatar>
              <div class="grid flex-1 text-left text-sm leading-tight">
                <span class="truncate font-semibold">{{ user.name }}</span>
                <span class="truncate text-xs">{{ user.email }}</span>
              </div>
            </div>
          </DropdownMenuLabel>
          <DropdownMenuSeparator />
  <DropdownMenuGroup>
    <DropdownMenuItem @click="openProfileDialog">
      <BadgeCheck />
      내 계정
    </DropdownMenuItem>
    <DropdownMenuItem>
      <Bell />
      내 알림
    </DropdownMenuItem>
    <DropdownMenuSub>
      <DropdownMenuSubTrigger>
        <Palette class="mr-2 h-4 w-4"/>
        테마 설정
      </DropdownMenuSubTrigger>
      <DropdownMenuPortal>
        <DropdownMenuSubContent>
          <DropdownMenuItem
            @click="setTheme('light')"
            :class="{ 'bg-accent': theme === 'light' }"
                  >
                    <Sun class="mr-2 h-4 w-4" />
                    <span>라이트 모드</span>
                    <Check v-if="theme === 'light'" class="ml-auto h-2 w-2" />
                  </DropdownMenuItem>
                  <DropdownMenuItem
                    @click="setTheme('dark')"
                    :class="{ 'bg-accent': theme === 'dark' }"
                  >
                    <Moon class="mr-2 h-4 w-4" />
                    <span>다크 모드</span>
                    <Check v-if="theme === 'dark'" class="ml-auto h-4 w-4" />
                  </DropdownMenuItem>
                  <DropdownMenuItem
                    @click="setTheme('system')"
                    :class="{ 'bg-accent': theme === 'system' }"
                  >
                    <Monitor class="mr-2 h-4 w-4" />
                    <span>시스템 설정</span>
                    <Check v-if="theme === 'system'" class="ml-auto h-4 w-4" />
                  </DropdownMenuItem>
                </DropdownMenuSubContent>
              </DropdownMenuPortal>
            </DropdownMenuSub>
          </DropdownMenuGroup>
          <DropdownMenuSeparator />
          <DropdownMenuItem @click="logout">
            <LogOut />
            로그아웃
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </SidebarMenuItem>
  </SidebarMenu>
</template>

<script setup lang="ts">
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuPortal,
  DropdownMenuSeparator,
  DropdownMenuSub,
  DropdownMenuSubContent,
  DropdownMenuSubTrigger,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { SidebarMenu, SidebarMenuButton, SidebarMenuItem } from '@/components/ui/sidebar';
import { BadgeCheck, Bell, Check, ChevronsUpDown, LogOut, Monitor, Moon, Palette, Sun } from 'lucide-vue-next';
import { useRouter } from 'vue-router';
import { useTheme } from '@/core/composables';

// Props
interface Props {
  user: {
    name: string;
    email: string;
    avatar: string;
  };
  onOpenProfileDialog?: () => void;
}

const props = withDefaults(defineProps<Props>(), {
  onOpenProfileDialog: () => {
  },
});

const { theme, setTheme } = useTheme();
const router = useRouter();

function openProfileDialog() {
  props.onOpenProfileDialog();
}

async function logout() {
  try {
    // AuthRepository를 사용한 로그아웃 로직은 원래 구현에 따라 추가
    localStorage.removeItem('user');
    localStorage.removeItem('employee');
    await router.push('/auths/login');
  } catch (error) {
    console.error('Logout error:', error);
  }
}
</script>
