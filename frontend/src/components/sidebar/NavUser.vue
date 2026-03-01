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
          <DropdownMenuItem @click="openLogoutDialog">
            <LogOut />
            로그아웃
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>

      <AlertDialog :open="isLogoutDialogOpen" @update:open="handleLogoutDialogOpenChange">
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>로그아웃 하시겠습니까?</AlertDialogTitle>
            <AlertDialogDescription>
              현재 세션이 종료되며 로그인 화면으로 이동합니다.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel
              :disabled="isLoggingOut"
              @click="handleLogoutDialogOpenChange(false)"
            >
              취소
            </AlertDialogCancel>
            <AlertDialogAction
              class="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              :disabled="isLoggingOut"
              @pointerdown.prevent
              @click="confirmLogout"
            >
              {{ isLoggingOut ? '로그아웃 중...' : '로그아웃' }}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </SidebarMenuItem>
  </SidebarMenu>
</template>

<script setup lang="ts">
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
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
import { ref } from 'vue';
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
const isLogoutDialogOpen = ref(false);
const isLoggingOut = ref(false);

function openProfileDialog() {
  props.onOpenProfileDialog();
}

function openLogoutDialog() {
  isLogoutDialogOpen.value = true;
}

function handleLogoutDialogOpenChange(value: boolean) {
  if (isLoggingOut.value && !value) {
    return;
  }
  isLogoutDialogOpen.value = value;
}

async function confirmLogout() {
  if (isLoggingOut.value) {
    return;
  }
  isLoggingOut.value = true;
  try {
    await authRepository.logout();
  } catch (error) {
    console.error('Logout error:', error);
  } finally {
    isLoggingOut.value = false;
    isLogoutDialogOpen.value = false;
    clearStoredUser();
    await router.push('/auths/login');
  }
}
</script>
