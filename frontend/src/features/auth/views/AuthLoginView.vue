<template>
  <Card class="w-full">
    <CardHeader class="space-y-1">
      <CardTitle class="text-2xl">로그인</CardTitle>
      <CardDescription>회사 계정으로 ABMS에 로그인하세요.</CardDescription>
    </CardHeader>

    <CardContent>
      <form class="space-y-4" @submit.prevent="submitLogin">
        <div class="space-y-2">
          <Label for="username">이메일</Label>
          <Input
            id="username"
            v-model="username"
            type="email"
            placeholder="name@iabacus.co.kr"
            autocomplete="email"
            required
          />
        </div>

        <div class="space-y-2">
          <Label for="password">비밀번호</Label>
          <Input
            id="password"
            v-model="password"
            type="password"
            autocomplete="current-password"
            required
          />
        </div>

        <Alert v-if="errorMessage" variant="destructive">
          <AlertTitle>로그인 실패</AlertTitle>
          <AlertDescription>{{ errorMessage }}</AlertDescription>
        </Alert>

        <Button type="submit" class="w-full" :disabled="isSubmitting">
          {{ isSubmitting ? '로그인 중...' : '로그인' }}
        </Button>

        <div class="text-center text-sm text-muted-foreground">
          계정이 없나요?
          <RouterLink class="font-medium text-primary underline-offset-4 hover:underline" to="/auths/register">
            회원가입 요청
          </RouterLink>
        </div>
      </form>
    </CardContent>
  </Card>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { toast } from 'vue-sonner';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { appContainer } from '@/core/di/container';
import HttpError from '@/core/http/HttpError';
import AuthRepository from '@/features/auth/repository/AuthRepository';
import { clearStoredUser, setStoredUser } from '@/features/auth/session';

const route = useRoute();
const router = useRouter();
const authRepository = appContainer.resolve(AuthRepository);

const username = ref('');
const password = ref('');
const errorMessage = ref<string | null>(null);
const isSubmitting = ref(false);

function getDisplayNameFromEmail(email: string): string {
  const localPart = email.split('@')[0]?.trim() ?? '';
  if (!localPart) {
    return 'User';
  }
  const normalized = localPart.replace(/[._-]+/g, ' ').trim();
  if (!normalized) {
    return 'User';
  }
  return normalized
    .split(' ')
    .map((token) => token.charAt(0).toUpperCase() + token.slice(1))
    .join(' ');
}

function resolveRedirectPath(): string {
  const redirect = route.query.redirect;
  if (typeof redirect !== 'string') {
    return '/';
  }
  if (!redirect.startsWith('/')) {
    return '/';
  }
  if (redirect.startsWith('/auths')) {
    return '/';
  }
  return redirect;
}

function validateInput(): string | null {
  if (!username.value.trim()) {
    return '이메일을 입력해 주세요.';
  }
  if (!password.value) {
    return '비밀번호를 입력해 주세요.';
  }
  return null;
}

async function submitLogin() {
  errorMessage.value = null;
  const validationError = validateInput();
  if (validationError) {
    errorMessage.value = validationError;
    return;
  }

  isSubmitting.value = true;
  try {
    const normalizedEmail = username.value.trim().toLowerCase();
    const displayName = getDisplayNameFromEmail(normalizedEmail);
    await authRepository.login({
      username: normalizedEmail,
      password: password.value,
    });

    let resolvedEmail = normalizedEmail;
    let resolvedName = displayName;
    try {
      const me = await authRepository.fetchMe();
      resolvedEmail = me.email || normalizedEmail;
      resolvedName = me.name || displayName;
    } catch {
      // Fallback to login input when /api/auth/me cannot be fetched.
    }

    setStoredUser({
      email: resolvedEmail,
      name: resolvedName,
    });

    await router.push(resolveRedirectPath());
  } catch (error) {
    clearStoredUser();
    const message =
      error instanceof HttpError ? error.message : '로그인 처리 중 오류가 발생했습니다.';
    errorMessage.value = message;
    toast.error('로그인에 실패했습니다.', { description: message });
  } finally {
    isSubmitting.value = false;
  }
}
</script>
