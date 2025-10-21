<template>
  <section class="flex h-full flex-col gap-6">
    <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
      <Card v-for="metric in metrics" :key="metric.title" class="shadow-none">
        <CardHeader class="space-y-1">
          <CardTitle class="text-sm font-medium text-muted-foreground">{{
            metric.title
          }}</CardTitle>
          <div class="flex items-baseline gap-2">
            <span class="text-2xl font-semibold text-foreground">{{ metric.value }}</span>
            <Badge :variant="metric.trend >= 0 ? 'secondary' : 'destructive'" class="text-xs">
              {{ metric.trend >= 0 ? '+' : '' }}{{ metric.trend.toFixed(1) }}%
            </Badge>
          </div>
        </CardHeader>
        <CardContent class="text-xs text-muted-foreground">
          {{ metric.description }}
        </CardContent>
      </Card>
    </div>

    <div class="grid gap-4 lg:grid-cols-3">
      <Card class="lg:col-span-2 shadow-none">
        <CardHeader>
          <CardTitle class="text-base">최근 알림</CardTitle>
          <CardDescription>시스템 전반에서 발생한 주요 이벤트입니다.</CardDescription>
        </CardHeader>
        <CardContent class="space-y-3">
          <div
            v-for="notification in notifications"
            :key="notification.id"
            class="flex items-start justify-between rounded-md border border-border/70 p-3 text-sm"
          >
            <div>
              <p class="font-medium text-foreground">{{ notification.title }}</p>
              <p class="text-xs text-muted-foreground">{{ notification.description }}</p>
            </div>
            <Badge variant="outline" class="text-[11px] uppercase tracking-tight">{{
              notification.time
            }}</Badge>
          </div>
          <p v-if="!notifications.length" class="text-sm text-muted-foreground">
            최근 알림이 없습니다.
          </p>
        </CardContent>
      </Card>

      <Card class="shadow-none">
        <CardHeader>
          <CardTitle class="text-base">빠른 작업</CardTitle>
          <CardDescription>자주 사용하는 화면으로 바로 이동하세요.</CardDescription>
        </CardHeader>
        <CardContent class="space-y-3">
          <router-link
            v-for="action in quickActions"
            :key="action.to"
            :to="action.to"
            class="flex items-center justify-between rounded-md border border-border/60 px-3 py-2 text-sm transition hover:bg-accent hover:text-accent-foreground"
          >
            <span class="font-medium">{{ action.label }}</span>
            <Badge variant="outline" class="text-[11px] uppercase tracking-tight">바로가기</Badge>
          </router-link>
        </CardContent>
      </Card>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';

const metrics = computed(() => [
  {
    title: '총 구성원',
    value: '128명',
    trend: 3.5,
    description: '지난달 대비 3.5% 증가했습니다.',
  },
  {
    title: '활성 프로젝트',
    value: '12건',
    trend: 1.2,
    description: '최신 진행 기준 활성 상태 프로젝트 수입니다.',
  },
  {
    title: '이번 달 신규 입사',
    value: '4명',
    trend: 0.0,
    description: '이번 달 입사 완료된 구성원 수입니다.',
  },
  {
    title: '휴가 중 구성원',
    value: '3명',
    trend: -1.5,
    description: '현재 휴직 또는 장기 휴가 중인 인원입니다.',
  },
]);

const notifications = computed(() => [
  {
    id: 'n-1',
    title: '조직 개편 완료',
    description: '경영기획실이 신설되어 조직도가 업데이트되었습니다.',
    time: '2시간 전',
  },
  {
    id: 'n-2',
    title: '연말 정산 안내',
    description: '전 직원 대상 연말 정산 일정이 공지되었습니다.',
    time: '어제',
  },
  {
    id: 'n-3',
    title: '교육 일정 등록',
    description: '1월 신규 입사자 교육 일정이 추가되었습니다.',
    time: '3일 전',
  },
]);

const quickActions = [
  { label: '조직도 확인', to: '/organization' },
  { label: '구성원 관리', to: '/employees' },
];
</script>
