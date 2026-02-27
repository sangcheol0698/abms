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
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { SidebarMenu, SidebarMenuButton, SidebarMenuItem } from '@/components/ui/sidebar';
import { appContainer } from '@/core/di/container';
import AuthRepository from '@/features/auth/repository/AuthRepository';
import { clearStoredUser } from '@/features/auth/session';
import { BadgeCheck, Bell, ChevronsUpDown, LogOut } from 'lucide-vue-next';
import { useRouter } from 'vue-router';

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

const router = useRouter();
const authRepository = appContainer.resolve(AuthRepository);

function openProfileDialog() {
  props.onOpenProfileDialog();
}

async function logout() {
  try {
    await authRepository.logout();
  } catch (error) {
    console.error('Logout error:', error);
  } finally {
    clearStoredUser();
    await router.push('/auths/login');
  }
}
</script>
