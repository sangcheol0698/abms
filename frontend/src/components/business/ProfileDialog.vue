<template>
  <Dialog :open="open" @update:open="handleOpenChange">
    <DialogContent
      class="max-h-[calc(100vh-1rem)] w-[calc(100%-1rem)] overflow-hidden p-0 md:max-h-[560px] md:max-w-[760px] lg:max-w-[860px]"
    >
      <DialogTitle class="sr-only">내 계정</DialogTitle>
      <DialogDescription class="sr-only">
        계정 정보를 확인하고 비밀번호를 변경할 수 있습니다.
      </DialogDescription>

      <SidebarProvider class="items-start">
        <Sidebar collapsible="none" class="hidden border-r bg-muted/20 md:flex">
          <SidebarContent class="p-3">
            <div class="px-2 py-3">
              <p class="text-sm font-semibold text-foreground">내 계정</p>
              <p class="mt-1 text-xs text-muted-foreground">
                계정, 알림, 환경 설정을 확인하고 관리합니다.
              </p>
            </div>

            <SidebarGroup>
              <SidebarGroupContent>
                <SidebarMenu>
                  <SidebarMenuItem v-for="item in navigationItems" :key="item.key">
                    <SidebarMenuButton
                      :is-active="activeSection === item.key"
                      @click="activeSection = item.key"
                    >
                      <component :is="item.icon" />
                      <span>{{ item.label }}</span>
                    </SidebarMenuButton>
                  </SidebarMenuItem>
                </SidebarMenu>
              </SidebarGroupContent>
            </SidebarGroup>
          </SidebarContent>
        </Sidebar>

        <main
          class="flex max-h-[calc(100vh-5rem)] min-h-0 flex-1 flex-col overflow-hidden md:h-[520px] md:max-h-none"
        >
          <header
            class="flex min-h-16 shrink-0 items-center border-b border-border bg-background px-4 py-3 pr-12"
          >
            <div class="flex min-w-0 flex-1 flex-col gap-3">
              <Breadcrumb>
                <BreadcrumbList>
                  <BreadcrumbItem class="hidden md:block">
                    <BreadcrumbPage>내 계정</BreadcrumbPage>
                  </BreadcrumbItem>
                  <BreadcrumbSeparator class="hidden md:block" />
                  <BreadcrumbItem>
                    <BreadcrumbPage>{{ activeSectionLabel }}</BreadcrumbPage>
                  </BreadcrumbItem>
                </BreadcrumbList>
              </Breadcrumb>

              <ToggleGroup
                class="w-full overflow-x-auto md:hidden"
                type="single"
                variant="outline"
                size="sm"
                :model-value="activeSection"
                @update:model-value="handleSectionChange"
              >
                <ToggleGroupItem
                  v-for="item in navigationItems"
                  :key="`mobile-${item.key}`"
                  :value="item.key"
                  class="min-w-fit px-3 text-xs"
                >
                  {{ item.label }}
                </ToggleGroupItem>
              </ToggleGroup>
            </div>
          </header>

          <div class="flex-1 overflow-y-auto">
            <section
              v-if="activeSection === 'profile'"
              class="flex flex-col gap-5 p-4 md:p-6"
              data-test="profile-section"
            >
              <div class="flex items-center gap-4 rounded-xl border border-border/60 bg-muted/20 p-4">
                <Avatar class="h-14 w-14">
                  <AvatarImage :src="user.avatar ?? ''" :alt="user.name" />
                  <AvatarFallback>{{ userInitials }}</AvatarFallback>
                </Avatar>
                <div class="min-w-0">
                  <p class="truncate text-base font-semibold text-foreground">{{ user.name }}</p>
                  <p class="truncate text-sm text-muted-foreground">{{ user.email }}</p>
                </div>
              </div>

              <div class="grid gap-4 md:grid-cols-2">
                <article class="rounded-xl border border-border/60 bg-background p-4 shadow-sm">
                  <p class="text-xs font-medium uppercase tracking-wide text-muted-foreground">
                    이름
                  </p>
                  <p class="mt-2 text-sm font-medium text-foreground">{{ user.name }}</p>
                </article>
                <article class="rounded-xl border border-border/60 bg-background p-4 shadow-sm">
                  <p class="text-xs font-medium uppercase tracking-wide text-muted-foreground">
                    이메일
                  </p>
                  <p class="mt-2 text-sm font-medium text-foreground">{{ user.email }}</p>
                </article>
              </div>

              <article class="rounded-xl border border-dashed border-border/70 bg-muted/10 p-4">
                <p class="text-sm font-semibold text-foreground">읽기 전용 정보</p>
                <p class="mt-2 text-xs leading-5 text-muted-foreground">
                  계정 이름과 이메일은 현재 직원 정보와 연동되어 있으며 이 화면에서는 수정하지
                  않습니다.
                </p>
              </article>
            </section>

            <section
              v-else-if="activeSection === 'security'"
              class="flex flex-col gap-5 p-4 md:p-6"
              data-test="security-section"
            >
              <div class="rounded-xl border border-border/60 bg-muted/20 p-4">
                <div class="flex items-start gap-3">
                  <div
                    class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-primary/10 text-primary"
                  >
                    <Lock class="h-4 w-4" />
                  </div>
                  <div class="space-y-2">
                    <p class="text-sm font-semibold text-foreground">비밀번호 변경</p>
                    <p class="text-xs leading-5 text-muted-foreground">
                      현재 비밀번호를 확인한 뒤 새 비밀번호로 변경합니다. 비밀번호는 8~64자이며
                      영문, 숫자, 특수문자를 각각 1자 이상 포함해야 합니다.
                    </p>
                  </div>
                </div>
              </div>

              <form
                id="change-password-form"
                class="grid gap-4 rounded-xl border border-border/60 bg-background p-4 shadow-sm"
                @submit.prevent="submitPasswordChange"
              >
                <div class="space-y-2">
                  <Label for="currentPassword">현재 비밀번호</Label>
                  <Input
                    id="currentPassword"
                    v-model="currentPassword"
                    type="password"
                    autocomplete="current-password"
                    :disabled="isChangingPassword"
                    required
                  />
                </div>

                <div class="space-y-2">
                  <Label for="newPassword">새 비밀번호</Label>
                  <Input
                    id="newPassword"
                    v-model="newPassword"
                    type="password"
                    autocomplete="new-password"
                    :disabled="isChangingPassword"
                    required
                  />
                </div>

                <div class="space-y-2">
                  <Label for="newPasswordConfirm">새 비밀번호 확인</Label>
                  <Input
                    id="newPasswordConfirm"
                    v-model="newPasswordConfirm"
                    type="password"
                    autocomplete="new-password"
                    :disabled="isChangingPassword"
                    required
                  />
                </div>

                <Alert v-if="passwordErrorMessage" variant="destructive">
                  <AlertTitle>비밀번호 변경 실패</AlertTitle>
                  <AlertDescription>{{ passwordErrorMessage }}</AlertDescription>
                </Alert>

                <div class="flex flex-col-reverse gap-2 pt-2 sm:flex-row sm:justify-between">
                  <Button
                    variant="destructive"
                    type="button"
                    size="sm"
                    class="sm:w-auto"
                    @click="openLogoutDialog"
                  >
                    로그아웃
                  </Button>
                  <Button
                    type="submit"
                    size="sm"
                    class="sm:w-auto"
                    :disabled="isChangingPassword"
                  >
                    {{ isChangingPassword ? '변경 중...' : '비밀번호 변경' }}
                  </Button>
                </div>
              </form>
            </section>

            <section
              v-else-if="activeSection === 'notifications'"
              class="flex flex-col gap-5 p-4 md:p-6"
              data-test="notifications-section"
            >
              <div class="rounded-xl border border-border/60 bg-muted/20 p-4">
                <div class="flex items-start gap-3">
                  <div
                    class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-primary/10 text-primary"
                  >
                    <Bell class="h-4 w-4" />
                  </div>
                  <div class="space-y-2">
                    <p class="text-sm font-semibold text-foreground">알림 설정</p>
                    <p class="text-xs leading-5 text-muted-foreground">
                      현재는 앱 내 알림을 중심으로 계정 알림 흐름을 안내합니다. 메일, 푸시, 읽지
                      않은 알림 관리 기능은 이후 단계에서 확장할 수 있습니다.
                    </p>
                  </div>
                </div>
              </div>

              <div class="grid gap-4 md:grid-cols-2">
                <article class="rounded-xl border border-border/60 bg-background p-4 shadow-sm">
                  <p class="text-xs font-medium uppercase tracking-wide text-muted-foreground">
                    현재 알림 채널
                  </p>
                  <p class="mt-2 text-sm font-medium text-foreground">앱 내 알림</p>
                  <p class="mt-2 text-xs leading-5 text-muted-foreground">
                    상단 알림 패널과 사이드 레이아웃의 알림 흐름을 통해 새 알림을 확인합니다.
                  </p>
                </article>

                <article class="rounded-xl border border-border/60 bg-background p-4 shadow-sm">
                  <p class="text-xs font-medium uppercase tracking-wide text-muted-foreground">
                    확장 예정 항목
                  </p>
                  <p class="mt-2 text-sm font-medium text-foreground">메일/푸시 알림</p>
                  <p class="mt-2 text-xs leading-5 text-muted-foreground">
                    중요 이벤트의 메일 발송, 알림 유형별 on/off, 읽음 일괄 처리 같은 옵션을 여기에
                    확장할 수 있습니다.
                  </p>
                </article>
              </div>

              <article class="rounded-xl border border-dashed border-border/70 bg-muted/10 p-4">
                <p class="text-sm font-semibold text-foreground">안내</p>
                <p class="mt-2 text-xs leading-5 text-muted-foreground">
                  이 섹션은 현재 계정의 알림 구조를 설명하는 정보 영역입니다. 실제 알림 on/off
                  제어는 아직 연결하지 않았습니다.
                </p>
              </article>
            </section>

            <section
              v-else-if="activeSection === 'preferences'"
              class="flex flex-col gap-5 p-4 md:p-6"
              data-test="preferences-section"
            >
              <div class="rounded-xl border border-border/60 bg-muted/20 p-4">
                <div class="flex items-start gap-3">
                  <div
                    class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-primary/10 text-primary"
                  >
                    <Paintbrush class="h-4 w-4" />
                  </div>
                  <div class="space-y-2">
                    <p class="text-sm font-semibold text-foreground">환경 설정</p>
                    <p class="text-xs leading-5 text-muted-foreground">
                      개인화 가능한 UI 설정의 현재 상태와 향후 확장 포인트를 정리합니다. 테마와
                      표시 환경처럼 사용성에 영향을 주는 항목을 여기서 다룹니다.
                    </p>
                  </div>
                </div>
              </div>

              <div class="grid gap-4 md:grid-cols-2">
                <article class="rounded-xl border border-border/60 bg-background p-4 shadow-sm">
                  <p class="text-xs font-medium uppercase tracking-wide text-muted-foreground">
                    테마
                  </p>
                  <p class="mt-2 text-sm font-medium text-foreground">상단 토글에서 변경</p>
                  <p class="mt-2 text-xs leading-5 text-muted-foreground">
                    현재 테마 전환은 전역 헤더의 테마 토글을 통해 관리됩니다. 향후 이 섹션에서 기본
                    테마 선호도와 시스템 연동 정책을 함께 다룰 수 있습니다.
                  </p>
                </article>

                <article class="rounded-xl border border-border/60 bg-background p-4 shadow-sm">
                  <p class="text-xs font-medium uppercase tracking-wide text-muted-foreground">
                    표시 환경
                  </p>
                  <p class="mt-2 text-sm font-medium text-foreground">언어/시간 형식 확장 예정</p>
                  <p class="mt-2 text-xs leading-5 text-muted-foreground">
                    언어, 날짜 형식, 숫자 표기, 기본 시작 화면 같은 사용자별 환경 설정은 이후 이
                    영역에 추가하는 것이 자연스럽습니다.
                  </p>
                </article>
              </div>

              <article class="rounded-xl border border-dashed border-border/70 bg-muted/10 p-4">
                <p class="text-sm font-semibold text-foreground">안내</p>
                <p class="mt-2 text-xs leading-5 text-muted-foreground">
                  이 섹션은 현재 환경 설정 구조를 설명하는 정보 영역입니다. 실제 저장 가능한 개인
                  설정은 아직 연결하지 않았습니다.
                </p>
              </article>
            </section>
          </div>
        </main>
      </SidebarProvider>
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
import type { AcceptableValue } from 'reka-ui';
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { Bell, Lock, Paintbrush, UserRound } from 'lucide-vue-next';
import { toast } from 'vue-sonner';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
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
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from '@/components/ui/breadcrumb';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogTitle,
} from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarProvider,
} from '@/components/ui/sidebar';
import { ToggleGroup, ToggleGroupItem } from '@/components/ui/toggle-group';
import { appContainer } from '@/core/di/container';
import HttpError from '@/core/http/HttpError';
import { useChangePasswordMutation } from '@/features/auth/queries/useAuthQueries';
import AuthRepository from '@/features/auth/repository/AuthRepository';
import { clearStoredUser } from '@/features/auth/session';

defineOptions({ name: 'ProfileDialog' });

type ProfileSection = 'profile' | 'security' | 'notifications' | 'preferences';

const navigationItems: Array<{
  key: ProfileSection;
  label: string;
  icon: typeof UserRound;
}> = [
  {
    key: 'profile',
    label: '계정 정보',
    icon: UserRound,
  },
  {
    key: 'security',
    label: '보안',
    icon: Lock,
  },
  {
    key: 'notifications',
    label: '알림 설정',
    icon: Bell,
  },
  {
    key: 'preferences',
    label: '환경 설정',
    icon: Paintbrush,
  },
];

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
const changePasswordMutation = useChangePasswordMutation();
const activeSection = ref<ProfileSection>('profile');
const isLogoutDialogOpen = ref(false);
const isLoggingOut = ref(false);
const currentPassword = ref('');
const newPassword = ref('');
const newPasswordConfirm = ref('');
const passwordErrorMessage = ref<string | null>(null);
const isChangingPassword = ref(false);
const STRONG_PASSWORD_PATTERN = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z\d]).{8,64}$/;

const userInitials = computed(() => props.user.name?.charAt(0)?.toUpperCase() ?? 'U');
const activeItem = computed(() => {
  const found = navigationItems.find((item) => item.key === activeSection.value);
  return found ?? navigationItems[0]!;
});
const activeSectionLabel = computed(() => activeItem.value.label);

function resetPasswordForm() {
  currentPassword.value = '';
  newPassword.value = '';
  newPasswordConfirm.value = '';
  passwordErrorMessage.value = null;
}

function handleOpenChange(value: boolean) {
  if (!value) {
    isLogoutDialogOpen.value = false;
    activeSection.value = 'profile';
    resetPasswordForm();
  }
  emit('update:open', value);
}

function openLogoutDialog() {
  isLogoutDialogOpen.value = true;
}

function handleSectionChange(value: AcceptableValue | AcceptableValue[]) {
  if (!value || Array.isArray(value)) {
    return;
  }
  activeSection.value = value as ProfileSection;
}

function handleLogoutDialogOpenChange(value: boolean) {
  if (isLoggingOut.value && !value) {
    return;
  }
  isLogoutDialogOpen.value = value;
}

function validatePasswordChange(): string | null {
  if (!currentPassword.value) {
    return '현재 비밀번호를 입력해 주세요.';
  }
  if (!newPassword.value) {
    return '새 비밀번호를 입력해 주세요.';
  }
  if (!STRONG_PASSWORD_PATTERN.test(newPassword.value)) {
    return '비밀번호는 영문, 숫자, 특수문자를 각각 1자 이상 포함해야 합니다.';
  }
  if (currentPassword.value === newPassword.value) {
    return '새 비밀번호는 현재 비밀번호와 달라야 합니다.';
  }
  if (!newPasswordConfirm.value) {
    return '새 비밀번호 확인을 입력해 주세요.';
  }
  if (newPassword.value !== newPasswordConfirm.value) {
    return '새 비밀번호가 일치하지 않습니다.';
  }
  return null;
}

async function submitPasswordChange() {
  passwordErrorMessage.value = null;
  const validationError = validatePasswordChange();
  if (validationError) {
    passwordErrorMessage.value = validationError;
    return;
  }

  isChangingPassword.value = true;
  try {
    await changePasswordMutation.mutateAsync({
      currentPassword: currentPassword.value,
      newPassword: newPassword.value,
    });
    resetPasswordForm();
    toast.success('비밀번호를 변경했습니다.');
  } catch (error) {
    const message =
      error instanceof HttpError ? error.message : '비밀번호 변경 중 오류가 발생했습니다.';
    passwordErrorMessage.value = message;
    toast.error('비밀번호 변경에 실패했습니다.', { description: message });
  } finally {
    isChangingPassword.value = false;
  }
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
