<template>
  <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
    <Card
      v-for="card in cards"
      :key="card.id"
      class="gap-3 py-4 shadow-sm"
    >
      <CardHeader class="space-y-1 px-5">
        <CardTitle class="text-sm font-semibold text-muted-foreground">
          {{ card.title }}
        </CardTitle>
        <div v-if="isLoading && !summary" class="h-8 rounded-md bg-muted/50 animate-pulse" />
        <div v-else class="text-2xl font-semibold text-foreground">
          {{ card.value }}
        </div>
      </CardHeader>
    </Card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { Card, CardHeader, CardTitle } from '@/components/ui/card';
import type { DashboardSummary } from '@/features/dashboard/models/dashboard';
import { formatCurrencyAmount } from '@/features/dashboard/utils/format';

const props = withDefaults(
  defineProps<{
    summary?: DashboardSummary | null;
    isLoading?: boolean;
    selectedYear?: number;
  }>(),
  {
    summary: null,
    isLoading: false,
    selectedYear: new Date().getFullYear(),
  },
);

const cards = computed(() => [
  {
    id: 'year-revenue',
    title: `${props.selectedYear}년 누적 매출`,
    value: formatCurrencyAmount(props.summary?.yearRevenue ?? 0),
  },
  {
    id: 'year-profit',
    title: `${props.selectedYear}년 누적 영업이익`,
    value: formatCurrencyAmount(props.summary?.yearProfit ?? 0),
  },
  {
    id: 'active-projects',
    title: '진행 중 프로젝트',
    value: `${(props.summary?.activeProjectsCount ?? 0).toLocaleString()}건`,
  },
  {
    id: 'current-employees',
    title: '현재 직원 수',
    value: `${(props.summary?.totalEmployeesCount ?? 0).toLocaleString()}명`,
  },
]);
</script>
