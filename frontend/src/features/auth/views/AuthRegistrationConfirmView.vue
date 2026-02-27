<template>
  <Card class="w-full">
    <CardHeader class="space-y-1">
      <CardTitle class="text-2xl">회원가입 확정</CardTitle>
      <CardDescription>새 비밀번호를 설정해 회원가입을 완료하세요.</CardDescription>
    </CardHeader>

    <CardContent class="space-y-4">
      <Alert v-if="!token" variant="destructive">
        <AlertTitle>유효하지 않은 접근</AlertTitle>
        <AlertDescription>
          회원가입 토큰이 없습니다. 회원가입 요청 화면에서 다시 링크를 발급해 주세요.
        </AlertDescription>
      </Alert>

      <form class="space-y-4" @submit.prevent="submitConfirmation">
        <div class="space-y-2">
          <Label for="password">비밀번호</Label>
          <Input
            id="password"
            v-model="password"
            type="password"
            autocomplete="new-password"
            :disabled="!token"
            required
          />
          <p class="text-xs text-muted-foreground">8자 이상으로 입력해 주세요.</p>
        </div>

        <div class="space-y-2">
          <Label for="passwordConfirm">비밀번호 확인</Label>
          <Input
            id="passwordConfirm"
            v-model="passwordConfirm"
            type="password"
            autocomplete="new-password"
            :disabled="!token"
            required
          />
        </div>

        <Alert v-if="errorMessage" variant="destructive">
          <AlertTitle>확정 실패</AlertTitle>
          <AlertDescription>{{ errorMessage }}</AlertDescription>
        </Alert>

        <Button type="submit" class="w-full" :disabled="isSubmitting || !token">
          {{ isSubmitting ? '처리 중...' : '회원가입 완료' }}
        </Button>
      </form>

      <div class="space-y-2">
        <Button class="w-full" variant="outline" as-child>
          <RouterLink to="/auths/register">회원가입 요청으로 이동</RouterLink>
        </Button>
        <Button class="w-full" variant="ghost" as-child>
          <RouterLink to="/auths/login">로그인으로 이동</RouterLink>
        </Button>
      </div>
    </CardContent>
  </Card>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
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

const route = useRoute();
const router = useRouter();
const authRepository = appContainer.resolve(AuthRepository);

const password = ref('');
const passwordConfirm = ref('');
const errorMessage = ref<string | null>(null);
const isSubmitting = ref(false);
const STRONG_PASSWORD_PATTERN = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z\d]).{8,64}$/;

const token = computed(() => {
  const value = route.query.token;
  if (typeof value === 'string') {
    const trimmed = value.trim();
    return trimmed.length > 0 ? trimmed : '';
  }
  return '';
});

function validateForm(): string | null {
  if (!token.value) {
    return '유효한 회원가입 토큰이 필요합니다.';
  }
  if (password.value.length < 8) {
    return '비밀번호는 8자 이상이어야 합니다.';
  }
  if (!STRONG_PASSWORD_PATTERN.test(password.value)) {
    return '비밀번호는 영문, 숫자, 특수문자를 각각 1자 이상 포함해야 합니다.';
  }
  if (password.value !== passwordConfirm.value) {
    return '비밀번호가 일치하지 않습니다.';
  }
  return null;
}

async function submitConfirmation() {
  errorMessage.value = null;
  const validationError = validateForm();
  if (validationError) {
    errorMessage.value = validationError;
    return;
  }

  isSubmitting.value = true;
  try {
    await authRepository.confirmRegistration({
      token: token.value,
      password: password.value,
    });

    toast.success('회원가입이 완료되었습니다. 로그인해 주세요.');
    await router.push('/auths/login');
  } catch (error) {
    const message =
      error instanceof HttpError ? error.message : '회원가입 확정 중 오류가 발생했습니다.';
    errorMessage.value = message;
    toast.error('회원가입 확정에 실패했습니다.', { description: message });
  } finally {
    isSubmitting.value = false;
  }
}
</script>
