<template>
  <Card class="w-full">
    <CardHeader class="space-y-1">
      <CardTitle class="text-2xl">비밀번호 찾기</CardTitle>
      <CardDescription>
        회사 이메일로 비밀번호 재설정 링크를 받을 수 있습니다.
      </CardDescription>
    </CardHeader>

    <CardContent class="space-y-4">
      <div v-if="isSent" class="space-y-4">
        <Alert>
          <AlertTitle>메일 확인 안내</AlertTitle>
          <AlertDescription>
            입력한 이메일이 가입된 계정이면 비밀번호 재설정 링크를 발송했습니다.
            메일의 링크는 30분 동안 사용할 수 있습니다.
          </AlertDescription>
        </Alert>

        <Button class="w-full" variant="outline" @click="isSent = false">다시 요청하기</Button>
        <Button class="w-full" as-child>
          <RouterLink to="/auths/login">로그인으로 이동</RouterLink>
        </Button>
      </div>

      <form v-else class="space-y-4" @submit.prevent="submitRequest">
        <div class="space-y-2">
          <Label for="email">회사 이메일</Label>
          <Input
            id="email"
            v-model="email"
            type="email"
            placeholder="name@iabacus.co.kr"
            autocomplete="email"
            required
          />
          <p class="text-xs text-muted-foreground">@iabacus.co.kr 도메인만 요청할 수 있습니다.</p>
        </div>

        <Alert v-if="errorMessage" variant="destructive">
          <AlertTitle>요청 실패</AlertTitle>
          <AlertDescription>{{ errorMessage }}</AlertDescription>
        </Alert>

        <Button type="submit" class="w-full" :disabled="isSubmitting">
          {{ isSubmitting ? '요청 중...' : '재설정 링크 요청' }}
        </Button>

        <div class="text-center text-sm text-muted-foreground">
          비밀번호가 기억나셨나요?
          <RouterLink class="font-medium text-primary underline-offset-4 hover:underline" to="/auths/login">
            로그인
          </RouterLink>
        </div>
      </form>
    </CardContent>
  </Card>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { toast } from 'vue-sonner';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import HttpError from '@/core/http/HttpError';
import { useRequestPasswordResetMutation } from '@/features/auth/queries/useAuthQueries';

const COMPANY_EMAIL_PATTERN = /^[A-Za-z0-9._%+-]+@iabacus\.co\.kr$/;

const requestPasswordResetMutation = useRequestPasswordResetMutation();

const email = ref('');
const errorMessage = ref<string | null>(null);
const isSubmitting = ref(false);
const isSent = ref(false);

function validateEmail(value: string): string | null {
  if (!value) {
    return '이메일을 입력해 주세요.';
  }
  if (!COMPANY_EMAIL_PATTERN.test(value)) {
    return '회사 이메일(@iabacus.co.kr)만 사용할 수 있습니다.';
  }
  return null;
}

async function submitRequest() {
  errorMessage.value = null;
  const normalizedEmail = email.value.trim().toLowerCase();
  const validationError = validateEmail(normalizedEmail);
  if (validationError) {
    errorMessage.value = validationError;
    return;
  }

  isSubmitting.value = true;
  try {
    await requestPasswordResetMutation.mutateAsync({ email: normalizedEmail });
    isSent.value = true;
    toast.success('비밀번호 재설정 안내를 확인해 주세요.');
  } catch (error) {
    const message =
      error instanceof HttpError ? error.message : '비밀번호 재설정 요청 중 오류가 발생했습니다.';
    errorMessage.value = message;
    toast.error('비밀번호 재설정 요청에 실패했습니다.', { description: message });
  } finally {
    isSubmitting.value = false;
  }
}
</script>
