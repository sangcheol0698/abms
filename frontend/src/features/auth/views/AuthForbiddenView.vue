<template>
  <Card class="w-full">
    <CardHeader class="space-y-1">
      <CardTitle class="text-2xl">접근 권한이 없습니다</CardTitle>
      <CardDescription>
        요청한 기능에 대한 권한이 없어 접근할 수 없습니다. 관리자에게 문의해 주세요.
      </CardDescription>
    </CardHeader>

    <CardContent class="space-y-3">
      <Button class="w-full" variant="outline" as-child>
        <RouterLink to="/">홈으로 이동</RouterLink>
      </Button>
      <Button class="w-full" variant="destructive" :disabled="isLoggingOut" @click="handleLogout">
        {{ isLoggingOut ? '로그아웃 중...' : '로그아웃' }}
      </Button>
    </CardContent>
  </Card>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { useLogoutMutation } from '@/features/auth/queries/useAuthQueries';
import { clearStoredUser } from '@/features/auth/session';

const router = useRouter();
const route = useRoute();
const logoutMutation = useLogoutMutation();
const isLoggingOut = computed(() => logoutMutation.isPending.value);

async function handleLogout() {
  try {
    await logoutMutation.mutateAsync();
  } catch (error) {
    console.error('Logout error:', error);
  } finally {
    clearStoredUser();
    await router.replace('/auths/login');
  }
}
</script>
