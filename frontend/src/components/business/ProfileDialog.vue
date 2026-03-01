<template>
  <Dialog :open="open" @update:open="handleOpenChange">
    <DialogContent>
      <DialogHeader>
        <DialogTitle>내 계정</DialogTitle>
        <DialogDescription
          >간단한 계정 정보를 확인하고 빠르게 로그아웃할 수 있습니다.</DialogDescription
        >
      </DialogHeader>

      <div class="space-y-4">
        <div class="flex items-center gap-3">
          <Avatar class="h-12 w-12">
            <AvatarImage :src="user.avatar ?? ''" :alt="user.name" />
            <AvatarFallback>{{ userInitials }}</AvatarFallback>
          </Avatar>
          <div>
            <p class="text-sm font-semibold text-foreground">{{ user.name }}</p>
            <p class="text-xs text-muted-foreground">{{ user.email }}</p>
          </div>
        </div>

        <div
          class="rounded-md border border-dashed border-border/60 bg-muted/40 p-4 text-xs text-muted-foreground"
        >
          <p>
            추가 계정 설정 화면은 구현 예정입니다. 상세 정보는 사용자 메뉴에서 확인할 수 있습니다.
          </p>
        </div>
      </div>

      <DialogFooter class="gap-2">
        <Button variant="outline" size="sm" @click="handleOpenChange(false)">닫기</Button>
        <Button variant="destructive" size="sm" @click="openLogoutDialog">로그아웃</Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>

  <AlertDialog :open="isLogoutDialogOpen" @update:open="handleLogoutDialogOpenChange">
    <AlertDialogContent>
      <AlertDialogHeader>
        <AlertDialogTitle>로그아웃 하시겠습니까?</AlertDialogTitle>
        <AlertDialogDescription>
          현재 세션이 종료되며 로그인 화면으로 이동합니다.
        </AlertDialogDescription>
      </AlertDialogHeader>
      <AlertDialogFooter>
        <AlertDialogCancel :disabled="isLoggingOut" @click="handleLogoutDialogOpenChange(false)">
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
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
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
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Button } from '@/components/ui/button';
import { appContainer } from '@/core/di/container';
import AuthRepository from '@/features/auth/repository/AuthRepository';
import { clearStoredUser } from '@/features/auth/session';

const props = withDefaults(
  defineProps<{
    open?: boolean;
    user?: {
      name: string;
      email: string;
      avatar?: string;
    };
  }>(),
  {
    open: false,
    user: () => ({ name: 'User', email: 'user@example.com', avatar: '' }),
  },
);

const emit = defineEmits<{
  (event: 'update:open', value: boolean): void;
}>();

const router = useRouter();
const authRepository = appContainer.resolve(AuthRepository);
const isLogoutDialogOpen = ref(false);
const isLoggingOut = ref(false);

const userInitials = computed(() => props.user.name?.charAt(0)?.toUpperCase() ?? 'U');

function handleOpenChange(value: boolean) {
  if (!value) {
    isLogoutDialogOpen.value = false;
  }
  emit('update:open', value);
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
    emit('update:open', false);
    await router.push('/auths/login');
  }
}
</script>
