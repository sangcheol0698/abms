<template>
  <Card>
    <CardHeader>
      <CardTitle>프로젝트 상태 현황</CardTitle>
      <CardDescription>{{ selectedYear }}년 기준 프로젝트 상태 분포입니다.</CardDescription>
    </CardHeader>
    <CardContent>
      <div
        v-if="isLoading && totalCount === 0"
        class="flex h-[240px] items-center justify-center text-sm text-muted-foreground"
      >
        데이터를 불러오는 중입니다...
      </div>
      <div
        v-else-if="totalCount === 0"
        class="flex h-[240px] items-center justify-center text-sm text-muted-foreground"
      >
        표시할 프로젝트 상태 데이터가 없습니다.
      </div>
      <div v-else class="space-y-5">
        <div v-for="status in statuses" :key="status.label" class="space-y-1.5">
          <div class="flex items-center justify-between text-sm">
            <span class="font-medium">{{ status.label }}</span>
            <span class="text-muted-foreground"
              >{{ status.count }}건 ({{ status.percentage }}%)</span
            >
          </div>
          <div class="h-2 w-full rounded-full bg-muted/50 overflow-hidden">
            <div
              class="h-full transition-all"
              :style="{ width: `${status.percentage}%`, backgroundColor: status.color }"
            />
          </div>
        </div>
      </div>
    </CardContent>
  </Card>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { getChartColor } from '@/core/theme/theme';
import { useDashboardProjectOverviewQuery } from '@/features/dashboard/queries/useDashboardQueries';

const props = withDefaults(
  defineProps<{
    selectedYear?: number;
  }>(),
  {
    selectedYear: new Date().getFullYear(),
  },
);

const projectOverviewQuery = useDashboardProjectOverviewQuery(computed(() => props.selectedYear));
const isLoading = computed(() => projectOverviewQuery.isLoading.value);
const summary = computed(() => projectOverviewQuery.data.value ?? null);
const totalCount = computed(() => summary.value?.totalCount ?? 0);

const statuses = computed(() => {
  const total = totalCount.value;

  return [
    { label: '예약', count: summary.value?.scheduledCount ?? 0, color: getChartColor(1) },
    { label: '진행 중', count: summary.value?.inProgressCount ?? 0, color: getChartColor(0) },
    { label: '완료', count: summary.value?.completedCount ?? 0, color: getChartColor(2) },
    { label: '보류', count: summary.value?.onHoldCount ?? 0, color: getChartColor(3) },
    { label: '취소', count: summary.value?.cancelledCount ?? 0, color: getChartColor(4) },
  ].map((status) => ({
    ...status,
    percentage: total > 0 ? Math.round((status.count / total) * 100) : 0,
  }));
});
</script>
