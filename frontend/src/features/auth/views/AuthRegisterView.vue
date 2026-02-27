<template>
  <Card class="w-full">
    <CardHeader class="space-y-1">
      <CardTitle class="text-2xl">회원가입 요청</CardTitle>
      <CardDescription>
        회사 이메일로 회원가입 링크를 받아 계정을 생성할 수 있습니다.
      </CardDescription>
    </CardHeader>

    <CardContent class="space-y-4">
      <div v-if="isSent" class="space-y-4">
        <Alert>
          <AlertTitle>메일 발송 완료</AlertTitle>
          <AlertDescription>
            <span class="font-medium">{{ requestedEmail }}</span> 주소로 회원가입 링크를 보냈습니다.
            메일의 링크를 클릭해 비밀번호를 설정해 주세요.
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
          {{ isSubmitting ? '요청 중...' : '회원가입 링크 요청' }}
        </Button>

        <div class="text-center text-sm text-muted-foreground">
          이미 계정이 있나요?
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
import { appContainer } from '@/core/di/container';
import HttpError from '@/core/http/HttpError';
import AuthRepository from '@/features/auth/repository/AuthRepository';

const COMPANY_EMAIL_PATTERN = /^[A-Za-z0-9._%+-]+@iabacus\.co\.kr$/;

const authRepository = appContainer.resolve(AuthRepository);

const email = ref('');
const requestedEmail = ref('');
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
    await authRepository.requestRegistration({ email: normalizedEmail });
    requestedEmail.value = normalizedEmail;
    isSent.value = true;
    toast.success('회원가입 링크를 발송했습니다.');
  } catch (error) {
    const message =
      error instanceof HttpError ? error.message : '회원가입 요청 중 오류가 발생했습니다.';
    errorMessage.value = message;
    toast.error('회원가입 요청에 실패했습니다.', { description: message });
  } finally {
    isSubmitting.value = false;
  }
}
</script>
