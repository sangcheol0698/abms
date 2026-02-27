<template>
  <Card class="w-full">
    <CardHeader class="space-y-1">
      <CardTitle class="text-2xl">세션이 만료되었습니다</CardTitle>
      <CardDescription>보안을 위해 다시 로그인해 주세요.</CardDescription>
    </CardHeader>

    <CardContent class="space-y-3">
      <Button class="w-full" as-child>
        <RouterLink :to="loginTarget">로그인 페이지로 이동</RouterLink>
      </Button>
      <Button class="w-full" variant="outline" as-child>
        <RouterLink to="/auths/register">회원가입 요청</RouterLink>
      </Button>
    </CardContent>
  </Card>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

const route = useRoute();

const loginTarget = computed(() => {
  const redirect = route.query.redirect;
  if (typeof redirect !== 'string' || !redirect.startsWith('/')) {
    return '/auths/login';
  }
  return {
    path: '/auths/login',
    query: { redirect },
  };
});
</script>
