<template>
  <Card class="w-full">
    <CardHeader class="space-y-1">
      <CardTitle class="text-2xl">비밀번호 재설정</CardTitle>
      <CardDescription>새 비밀번호를 설정해 계정 접근을 복구하세요.</CardDescription>
    </CardHeader>

    <CardContent class="space-y-4">
      <Alert v-if="!token" variant="destructive">
        <AlertTitle>유효하지 않은 접근</AlertTitle>
        <AlertDescription>
          비밀번호 재설정 토큰이 없습니다. 비밀번호 찾기 화면에서 다시 링크를 발급해 주세요.
        </AlertDescription>
      </Alert>

      <form class="space-y-4" @submit.prevent="submitConfirmation">
        <div class="space-y-2">
          <Label for="password">새 비밀번호</Label>
          <Input
            id="password"
            v-model="password"
            type="password"
            autocomplete="new-password"
            :disabled="!token"
            required
          />
          <p class="text-xs text-muted-foreground">
            8~64자이며 영문, 숫자, 특수문자를 각각 1자 이상 포함해야 합니다.
          </p>
        </div>

        <div class="space-y-2">
          <Label for="passwordConfirm">새 비밀번호 확인</Label>
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
          <AlertTitle>재설정 실패</AlertTitle>
          <AlertDescription>{{ errorMessage }}</AlertDescription>
        </Alert>

        <Button type="submit" class="w-full" :disabled="isSubmitting || !token">
          {{ isSubmitting ? '처리 중...' : '비밀번호 재설정' }}
        </Button>
      </form>

      <div class="space-y-2">
        <Button class="w-full" variant="outline" as-child>
          <RouterLink to="/auths/password-reset">비밀번호 찾기로 이동</RouterLink>
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
import HttpError from '@/core/http/HttpError';
import { useConfirmPasswordResetMutation } from '@/features/auth/queries/useAuthQueries';

const route = useRoute();
const router = useRouter();
const confirmPasswordResetMutation = useConfirmPasswordResetMutation();

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
    return '유효한 비밀번호 재설정 토큰이 필요합니다.';
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
    await confirmPasswordResetMutation.mutateAsync({
      token: token.value,
      password: password.value,
    });

    toast.success('비밀번호를 재설정했습니다. 새 비밀번호로 로그인해 주세요.');
    await router.push('/auths/login');
  } catch (error) {
    const message =
      error instanceof HttpError ? error.message : '비밀번호 재설정 중 오류가 발생했습니다.';
    errorMessage.value = message;
    toast.error('비밀번호 재설정에 실패했습니다.', { description: message });
  } finally {
    isSubmitting.value = false;
  }
}
</script>
