<template>
  <Card class="w-full">
    <CardHeader class="space-y-1">
      <CardTitle class="text-2xl">접근 권한이 없습니다</CardTitle>
      <CardDescription>
        요청한 기능에 대한 권한이 없어 접근할 수 없습니다. 관리자에게 문의해 주세요.
      </CardDescription>
    </CardHeader>

    <CardContent class="space-y-3">
      <Button class="w-full" @click="goBack">이전 페이지로</Button>
      <Button class="w-full" variant="outline" as-child>
        <RouterLink to="/">홈으로 이동</RouterLink>
      </Button>
    </CardContent>
  </Card>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

const router = useRouter();

async function goBack() {
  if (window.history.length > 1) {
    await router.back();
    return;
  }
  await router.push('/');
}
</script>
